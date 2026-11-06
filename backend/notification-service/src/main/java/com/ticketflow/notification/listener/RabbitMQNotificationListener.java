package com.ticketflow.notification.listener;

import com.ticketflow.common.events.BookingCancelledEvent;
import com.ticketflow.common.events.BookingConfirmedEvent;
import com.ticketflow.common.events.PaymentRefundedEvent;
import com.ticketflow.notification.service.TicketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationListener {

    private final TicketNotificationService notificationService;

    @RabbitListener(queues = "${ticketflow.rabbitmq.booking-confirmed-queue:notification.booking.confirmed.queue}")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received RabbitMQ BookingConfirmedEvent: bookingRef={}, movie={}",
                event.getBookingReference(), event.getMovieTitle());
        try {
            notificationService.sendBookingConfirmation(event);
        } catch (Exception e) {
            log.error("Failed to process BookingConfirmedEvent for ref {}: {}", event.getBookingReference(),
                    e.getMessage());
        }
    }

    @RabbitListener(queues = "${ticketflow.rabbitmq.booking-cancelled-queue:notification.booking.cancelled.queue}")
    public void handleBookingCancelled(BookingCancelledEvent event) {
        log.info("Received RabbitMQ BookingCancelledEvent: bookingId={}, reason={}",
                event.getBookingId(), event.getReason());
        try {
            notificationService.sendBookingCancellation(event);
        } catch (Exception e) {
            log.error("Failed to process BookingCancelledEvent for booking {}: {}", event.getBookingId(), e.getMessage());
        }
    }

    @RabbitListener(queues = "${ticketflow.rabbitmq.payment-refunded-queue:notification.payment.refunded.queue}")
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        log.info("Received RabbitMQ PaymentRefundedEvent: refundId={}, amount={}",
                event.getRefundId(), event.getRefundedAmount());
        try {
            notificationService.sendPaymentRefundNotification(event);
        } catch (Exception e) {
            log.error("Failed to process PaymentRefundedEvent for refund {}: {}", event.getRefundId(), e.getMessage());
        }
    }
}
