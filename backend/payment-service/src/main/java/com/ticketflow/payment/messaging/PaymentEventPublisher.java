package com.ticketflow.payment.messaging;

import com.ticketflow.common.events.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String EXCHANGE_NAME = "ticketflow.topic.exchange";

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent: tx={}, booking={}, status={}",
                event.getTransactionId(), event.getBookingId(), event.getStatus());
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "payment.processed", event);
        } catch (Exception e) {
            log.warn("Failed to publish payment event to RabbitMQ: {}", e.getMessage());
        }
    }
}

