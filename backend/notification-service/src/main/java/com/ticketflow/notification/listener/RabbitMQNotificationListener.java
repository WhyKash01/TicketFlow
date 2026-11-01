package com.ticketflow.notification.listener;

import com.ticketflow.common.events.BookingConfirmedEvent;
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

    @RabbitListener(queues = "ticketflow.notification.queue")
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Received RabbitMQ BookingConfirmedEvent: bookingRef={}", event.getBookingReference());
        notificationService.sendBookingConfirmation(event);
    }
}

