package com.ticketflow.payment.service;

import com.ticketflow.common.dto.PaymentRequest;
import com.ticketflow.common.dto.PaymentResponse;
import com.ticketflow.common.enums.PaymentStatus;
import com.ticketflow.common.events.PaymentProcessedEvent;
import com.ticketflow.payment.messaging.PaymentEventPublisher;
import com.ticketflow.payment.model.PaymentTransaction;
import com.ticketflow.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher eventPublisher;
    private final RestTemplate restTemplate;

    @Value("${ticketflow.booking-service.url:http://localhost:8082}")
    private String bookingServiceUrl;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for bookingId: {}, amount: {}", request.getBookingId(), request.getAmount());

        // 1. Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<PaymentTransaction> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Idempotent hit for key: {}", request.getIdempotencyKey());
                return mapToResponse(existing.get());
            }
        }

        String transactionId = "TX-" + UUID.randomUUID().toString();
        String gatewayRef = "GATEWAY-" + System.currentTimeMillis();

        // 2. Mock Payment processing / failure simulation for testing Saga
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
                .gatewayReference(gatewayRef)
                .idempotencyKey(request.getIdempotencyKey())
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentTransaction saved = paymentRepository.save(transaction);

        // 3. Coordinate with Booking Service (Saga next step or compensation)
        try {
            if (isSuccess) {
                String confirmUrl = bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/confirm?transactionId=" + transactionId;
                restTemplate.postForObject(confirmUrl, null, String.class);
                log.info("Notified booking service of payment success for {}", request.getBookingId());
            } else {
                String cancelUrl = bookingServiceUrl + "/api/bookings/" + request.getBookingId() + "/cancel?reason=" + failureReason;
                restTemplate.postForObject(cancelUrl, null, String.class);
                log.warn("Notified booking service of payment failure / rollback for {}", request.getBookingId());
            }
        } catch (Exception e) {
            log.warn("Could not directly call booking service (is booking service online?): {}", e.getMessage());
        }

        // 4. Publish Event to RabbitMQ
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

    public Optional<PaymentTransaction> getPaymentById(String transactionId) {
        return paymentRepository.findById(transactionId);
    }

    public Optional<PaymentTransaction> getPaymentByBookingId(String bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }

    private PaymentResponse mapToResponse(PaymentTransaction tx) {
        return PaymentResponse.builder()
                .transactionId(tx.getTransactionId())
                .bookingId(tx.getBookingId())
                .amount(tx.getAmount())
                .status(tx.getStatus())
                .gatewayReference(tx.getGatewayReference())
                .failureReason(tx.getFailureReason())
                .timestamp(tx.getCreatedAt())
                .build();
    }
}

