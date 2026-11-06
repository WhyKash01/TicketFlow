package com.ticketflow.payment.service;

import com.ticketflow.common.dto.PaymentRequest;
import com.ticketflow.common.dto.PaymentResponse;
import com.ticketflow.common.dto.RefundRequest;
import com.ticketflow.common.dto.RefundResponse;
import com.ticketflow.common.enums.PaymentProvider;
import com.ticketflow.common.enums.PaymentStatus;
import com.ticketflow.common.events.PaymentProcessedEvent;
import com.ticketflow.common.events.PaymentRefundedEvent;
import com.ticketflow.payment.messaging.PaymentEventPublisher;
import com.ticketflow.payment.model.PaymentTransaction;
import com.ticketflow.payment.model.RefundLog;
import com.ticketflow.payment.repository.PaymentRepository;
import com.ticketflow.payment.repository.RefundLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundLogRepository refundLogRepository;
    private final PaymentEventPublisher eventPublisher;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ticketflow.booking-service.url:http://localhost:8082}")
    private String bookingServiceUrl;

    /**
     * SAGA Step 3: Process Payment with Redis Idempotency Protection.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for bookingId: {}, amount: {}, provider: {}",
                request.getBookingId(), request.getAmount(), request.getProvider());

        String idempotencyKey = request.getIdempotencyKey();
        String redisKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? "idempotency:pay:" + idempotencyKey
                : null;

        // 1. Fast Redis Idempotency Check
        if (redisKey != null) {
            try {
                String cachedTxId = stringRedisTemplate.opsForValue().get(redisKey);
                if (cachedTxId != null && !cachedTxId.equals("IN_PROGRESS")) {
                    log.info("Redis Idempotency hit for key: {} -> tx: {}", idempotencyKey, cachedTxId);
                    Optional<PaymentTransaction> cachedTx = paymentRepository.findById(cachedTxId);
                    if (cachedTx.isPresent()) {
                        return mapToResponse(cachedTx.get());
                    }
                }

                // Acquire in-flight lock to guard against simultaneous double-submissions
                Boolean lockAcquired = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "IN_PROGRESS", Duration.ofMinutes(3));
                if (Boolean.FALSE.equals(lockAcquired)) {
                    log.warn("Concurrent duplicate payment detected for key: {}", idempotencyKey);
                    Optional<PaymentTransaction> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
                    if (existing.isPresent()) {
                        return mapToResponse(existing.get());
                    }
                }
            } catch (Exception e) {
                log.warn("Redis idempotency check warning: {}", e.getMessage());
            }

            // Database secondary idempotency guard
            Optional<PaymentTransaction> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("DB Idempotency hit for key: {}", idempotencyKey);
                return mapToResponse(existing.get());
            }
        }

        String transactionId = "TX-" + UUID.randomUUID().toString();
        PaymentProvider provider = request.getProvider() != null ? request.getProvider() : PaymentProvider.MOCK;
        String gatewayRef = provider.name() + "-GW-" + System.currentTimeMillis();

        // 2. Gateway execution / Failure simulation for Saga testing
        boolean isSuccess = !request.isSimulateFailure();
        PaymentStatus finalStatus = isSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        String failureReason = isSuccess ? null : "Simulated bank payment decline (Saga compensation test)";

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionId(transactionId)
                .bookingId(request.getBookingId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "UPI")
                .status(finalStatus)
                .provider(provider)
                .gatewayReference(gatewayRef)
                .idempotencyKey(idempotencyKey)
                .failureReason(failureReason)
                .refundedAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentTransaction saved = paymentRepository.save(transaction);

        // 3. Cache confirmed transaction in Redis with 24-hour TTL
        if (redisKey != null) {
            try {
                stringRedisTemplate.opsForValue().set(redisKey, saved.getTransactionId(), Duration.ofHours(24));
            } catch (Exception e) {
                log.warn("Failed to store payment key in Redis cache: {}", e.getMessage());
            }
        }

        // 4. Synchronous Saga step coordination with Booking Service
        try {
            if (isSuccess) {
                String confirmUrl = bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/confirm?transactionId=" + transactionId;
                restTemplate.postForObject(confirmUrl, null, String.class);
                log.info("Notified booking service of payment success for bookingId: {}", request.getBookingId());
            } else {
                String cancelUrl = bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/cancel?reason=" + failureReason;
                restTemplate.postForObject(cancelUrl, null, String.class);
                log.warn("Notified booking service of payment failure / rollback for bookingId: {}", request.getBookingId());
            }
        } catch (Exception e) {
            log.warn("Could not directly call booking service (is booking service online?): {}", e.getMessage());
        }

        // 5. Publish Event to RabbitMQ topic exchange
        eventPublisher.publishPaymentProcessed(PaymentProcessedEvent.builder()
                .transactionId(saved.getTransactionId())
                .bookingId(saved.getBookingId())
                .userId(saved.getUserId())
                .amount(saved.getAmount())
                .status(saved.getStatus())
                .failureReason(saved.getFailureReason())
                .build());

        return mapToResponse(saved);
    }

    /**
     * SAGA Compensating Action: Process refund for cancelled or rollback bookings.
     */
    @Transactional
    public RefundResponse processRefund(RefundRequest request) {
        log.info("Initiating refund for bookingId: {}, txId: {}, amount: {}",
                request.getBookingId(), request.getTransactionId(), request.getAmount());

        String idempotencyKey = request.getIdempotencyKey();
        String redisKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? "idempotency:refund:" + idempotencyKey
                : null;

        // 1. Idempotency Check for Refund
        if (redisKey != null) {
            try {
                String cachedRefundId = stringRedisTemplate.opsForValue().get(redisKey);
                if (cachedRefundId != null) {
                    Optional<RefundLog> cachedLog = refundLogRepository.findById(cachedRefundId);
                    if (cachedLog.isPresent()) {
                        return mapToRefundResponse(cachedLog.get());
                    }
                }
            } catch (Exception e) {
                log.warn("Redis refund idempotency warning: {}", e.getMessage());
            }

            Optional<RefundLog> existing = refundLogRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return mapToRefundResponse(existing.get());
            }
        }

        // 2. Fetch original transaction
        PaymentTransaction tx;
        if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
            tx = paymentRepository.findById(request.getTransactionId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found: " + request.getTransactionId()));
        } else if (request.getBookingId() != null && !request.getBookingId().isBlank()) {
            tx = paymentRepository.findByBookingId(request.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("No payment transaction found for booking: " + request.getBookingId()));
        } else {
            throw new IllegalArgumentException("Either transactionId or bookingId must be provided for refund.");
        }

        if (tx.getStatus() != PaymentStatus.SUCCESS && tx.getStatus() != PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot refund transaction in status " + tx.getStatus());
        }

        // 3. Amount calculations and validation
        BigDecimal currentRefunded = tx.getRefundedAmount() != null ? tx.getRefundedAmount() : BigDecimal.ZERO;
        BigDecimal requestedAmount = (request.getAmount() != null)
                ? request.getAmount()
                : tx.getAmount().subtract(currentRefunded);

        if (requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than zero.");
        }

        BigDecimal newTotalRefunded = currentRefunded.add(requestedAmount);
        if (newTotalRefunded.compareTo(tx.getAmount()) > 0) {
            throw new IllegalArgumentException(String.format("Total refund amount (%.2f) exceeds original transaction amount (%.2f)",
                    newTotalRefunded, tx.getAmount()));
        }

        // 4. Execute gateway refund reference
        String refundRef = "REF-" + tx.getProvider().name() + "-" + System.currentTimeMillis();
        String refundId = UUID.randomUUID().toString();

        tx.setRefundedAmount(newTotalRefunded);
        tx.setRefundReference(refundRef);
        tx.setRefundReason(request.getReason());
        if (newTotalRefunded.compareTo(tx.getAmount()) >= 0) {
            tx.setStatus(PaymentStatus.REFUNDED);
        }
        paymentRepository.save(tx);

        // 5. Persist Refund Audit Log
        RefundLog refundLog = RefundLog.builder()
                .refundId(refundId)
                .transactionId(tx.getTransactionId())
                .bookingId(tx.getBookingId())
                .refundAmount(requestedAmount)
                .refundReference(refundRef)
                .reason(request.getReason())
                .idempotencyKey(idempotencyKey)
                .status("REFUND_SUCCESS")
                .createdAt(LocalDateTime.now())
                .build();
        RefundLog savedLog = refundLogRepository.save(refundLog);

        // 6. Cache in Redis
        if (redisKey != null) {
            try {
                stringRedisTemplate.opsForValue().set(redisKey, savedLog.getRefundId(), Duration.ofHours(24));
            } catch (Exception e) {
                log.warn("Failed to store refund key in Redis: {}", e.getMessage());
            }
        }

        // 7. Publish domain event to RabbitMQ
        eventPublisher.publishPaymentRefunded(PaymentRefundedEvent.builder()
                .refundId(savedLog.getRefundId())
                .transactionId(tx.getTransactionId())
                .bookingId(tx.getBookingId())
                .userId(tx.getUserId())
                .refundedAmount(requestedAmount)
                .reason(request.getReason())
                .build());

        log.info("Saga Compensation: Refund of {} completed for bookingId: {} (ref: {})",
                requestedAmount, tx.getBookingId(), refundRef);

        return mapToRefundResponse(savedLog);
    }

    public Optional<PaymentTransaction> getPaymentById(String transactionId) {
        return paymentRepository.findById(transactionId);
    }

    public Optional<PaymentTransaction> getPaymentByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    public List<RefundLog> getRefundsByTransactionId(String transactionId) {
        return refundLogRepository.findByTransactionIdOrderByCreatedAtDesc(transactionId);
    }

    public List<RefundLog> getRefundsByBookingId(String bookingId) {
        return refundLogRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    private PaymentResponse mapToResponse(PaymentTransaction tx) {
        return PaymentResponse.builder()
                .transactionId(tx.getTransactionId())
                .bookingId(tx.getBookingId())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .provider(tx.getProvider())
                .gatewayReference(tx.getGatewayReference())
                .failureReason(tx.getFailureReason())
                .refundReference(tx.getRefundReference())
                .refundedAmount(tx.getRefundedAmount())
                .timestamp(tx.getCreatedAt())
                .build();
    }

    private RefundResponse mapToRefundResponse(RefundLog log) {
        return RefundResponse.builder()
                .refundId(log.getRefundId())
                .transactionId(log.getTransactionId())
                .bookingId(log.getBookingId())
                .refundAmount(log.getRefundAmount())
                .refundReference(log.getRefundReference())
                .status(log.getStatus())
                .reason(log.getReason())
                .timestamp(log.getCreatedAt())
                .build();
    }
}
