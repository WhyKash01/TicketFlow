package com.ticketflow.payment.messaging;

import com.ticketflow.common.events.PaymentProcessedEvent;
import com.ticketflow.common.events.PaymentRefundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${ticketflow.rabbitmq.exchange:ticketflow.topic.exchange}")
    private String exchangeName;

    @Value("${ticketflow.rabbitmq.payment-routing-key:payment.processed}")
    private String paymentRoutingKey;

    @Value("${ticketflow.rabbitmq.refund-routing-key:payment.refunded}")
    private String refundRoutingKey;

    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Publishing PaymentProcessedEvent: tx={}, booking={}, status={}",
                event.getTransactionId(), event.getBookingId(), event.getStatus());
        try {
            rabbitTemplate.convertAndSend(exchangeName, paymentRoutingKey, event);
        } catch (Exception e) {
            log.warn("Failed to publish payment event to RabbitMQ: {}", e.getMessage());
        }
    }

    public void publishPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Publishing PaymentRefundedEvent: refundId={}, tx={}, booking={}, amount={}",
                event.getRefundId(), event.getTransactionId(), event.getBookingId(), event.getRefundedAmount());
        try {
            rabbitTemplate.convertAndSend(exchangeName, refundRoutingKey, event);
        } catch (Exception e) {
            log.warn("Failed to publish refund event to RabbitMQ: {}", e.getMessage());
        }
    }
}
