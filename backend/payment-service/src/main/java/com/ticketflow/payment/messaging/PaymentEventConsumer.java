package com.ticketflow.payment.messaging;

import com.ticketflow.common.dto.RefundRequest;
import com.ticketflow.common.enums.PaymentStatus;
import com.ticketflow.common.events.BookingCancelledEvent;
import com.ticketflow.payment.model.PaymentTransaction;
import com.ticketflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    /**
     * Listens for BookingCancelledEvent to perform Saga compensating refund automatically
     * if the booking was already paid for before cancellation or expiration.
     */
    @RabbitListener(queues = "${ticketflow.rabbitmq.booking-cancelled-queue:payment.booking.cancelled.queue}")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Received BookingCancelledEvent for bookingId: {}, reason: {}", event.getBookingId(), event.getReason());

        if (event.getBookingId() == null || event.getBookingId().isBlank()) {
            return;
        }

        try {
            Optional<PaymentTransaction> txOpt = paymentService.getPaymentByBookingId(event.getBookingId());
            if (txOpt.isPresent()) {
                PaymentTransaction tx = txOpt.get();
                if (tx.getStatus() == PaymentStatus.SUCCESS) {
                    log.info("Triggering automatic Saga compensating refund for booking: {}", event.getBookingId());
                    paymentService.processRefund(RefundRequest.builder()
                            .bookingId(event.getBookingId())
                            .transactionId(tx.getTransactionId())
                            .reason("Automatic Saga compensation: " + event.getReason())
                            .idempotencyKey("saga-compensate-" + event.getBookingId())
                            .build());
                } else {
                    log.info("Booking {} payment status is {}, no refund required.", event.getBookingId(), tx.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process Saga compensating refund for booking {}: {}", event.getBookingId(), e.getMessage());
        }
    }
}

