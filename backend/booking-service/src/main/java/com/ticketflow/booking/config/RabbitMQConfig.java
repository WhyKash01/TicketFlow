package com.ticketflow.booking.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "ticketflow.topic.exchange";
    public static final String BOOKING_QUEUE = "ticketflow.booking.queue";
    public static final String NOTIFICATION_QUEUE = "ticketflow.notification.queue";
    public static final String ROUTING_KEY_BOOKING = "booking.#";

    @Bean
    public TopicExchange ticketflowExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue bookingQueue() {
        return QueueBuilder.durable(BOOKING_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding bookingBinding(Queue bookingQueue, TopicExchange ticketflowExchange) {
        return BindingBuilder.bind(bookingQueue).to(ticketflowExchange).with("booking.*");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange ticketflowExchange) {
        return BindingBuilder.bind(notificationQueue).to(ticketflowExchange).with("#");
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

