package com.ticketflow.booking.messaging;

import com.ticketflow.booking.config.RabbitMQConfig;
import com.ticketflow.common.events.BookingCancelledEvent;
import com.ticketflow.common.events.BookingConfirmedEvent;
import com.ticketflow.common.events.BookingCreatedEvent;
import com.ticketflow.common.events.SeatLockedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSeatLocked(SeatLockedEvent event) {
        log.info("Publishing SeatLockedEvent: lockId={}, showId={}", event.getLockId(), event.getShowId());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.seat.locked", event);
        } catch (Exception e) {
            log.warn("Failed to publish to RabbitMQ (is broker running?): {}", e.getMessage());
        }
    }

    public void publishBookingCreated(BookingCreatedEvent event) {
        log.info("Publishing BookingCreatedEvent: bookingId={}", event.getBookingId());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.created", event);
        } catch (Exception e) {
            log.warn("Failed to publish to RabbitMQ: {}", e.getMessage());
        }
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Publishing BookingConfirmedEvent: bookingReference={}", event.getBookingReference());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.confirmed", event);
        } catch (Exception e) {
            log.warn("Failed to publish to RabbitMQ: {}", e.getMessage());
        }
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        log.info("Publishing BookingCancelledEvent: bookingId={}", event.getBookingId());
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "booking.cancelled", event);
        } catch (Exception e) {
            log.warn("Failed to publish to RabbitMQ: {}", e.getMessage());
        }
    }
}

