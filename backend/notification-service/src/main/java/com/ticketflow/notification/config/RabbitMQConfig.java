package com.ticketflow.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${ticketflow.rabbitmq.exchange:ticketflow.topic.exchange}")
    private String exchangeName;

    @Value("${ticketflow.rabbitmq.booking-confirmed-queue:notification.booking.confirmed.queue}")
    private String bookingConfirmedQueue;

    @Value("${ticketflow.rabbitmq.booking-cancelled-queue:notification.booking.cancelled.queue}")
    private String bookingCancelledQueue;

    @Value("${ticketflow.rabbitmq.payment-refunded-queue:notification.payment.refunded.queue}")
    private String paymentRefundedQueue;

    @Value("${ticketflow.rabbitmq.legacy-notification-queue:ticketflow.notification.queue}")
    private String legacyNotificationQueue;

    @Bean
    public TopicExchange notificationTopicExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue bookingConfirmedQueue() {
        return QueueBuilder.durable(bookingConfirmedQueue).build();
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(bookingCancelledQueue).build();
    }

    @Bean
    public Queue paymentRefundedQueue() {
        return QueueBuilder.durable(paymentRefundedQueue).build();
    }

    @Bean
    public Queue legacyNotificationQueue() {
        return QueueBuilder.durable(legacyNotificationQueue).build();
    }

    @Bean
    public Binding bookingConfirmedBinding(Queue bookingConfirmedQueue, TopicExchange notificationTopicExchange) {
        return BindingBuilder.bind(bookingConfirmedQueue).to(notificationTopicExchange).with("booking.confirmed");
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange notificationTopicExchange) {
        return BindingBuilder.bind(bookingCancelledQueue).to(notificationTopicExchange).with("booking.cancelled");
    }

    @Bean
    public Binding paymentRefundedBinding(Queue paymentRefundedQueue, TopicExchange notificationTopicExchange) {
        return BindingBuilder.bind(paymentRefundedQueue).to(notificationTopicExchange).with("payment.refunded");
    }

    @Bean
    public Binding legacyNotificationBinding(Queue legacyNotificationQueue, TopicExchange notificationTopicExchange) {
        return BindingBuilder.bind(legacyNotificationQueue).to(notificationTopicExchange).with("#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

