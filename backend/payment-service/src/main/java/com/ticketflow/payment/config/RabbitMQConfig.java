package com.ticketflow.payment.config;

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

    @Value("${ticketflow.rabbitmq.booking-cancelled-queue:payment.booking.cancelled.queue}")
    private String bookingCancelledQueue;

    @Bean
    public TopicExchange paymentTopicExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Queue bookingCancelledQueue() {
        return QueueBuilder.durable(bookingCancelledQueue).build();
    }

    @Bean
    public Binding bookingCancelledBinding(Queue bookingCancelledQueue, TopicExchange paymentTopicExchange) {
        return BindingBuilder.bind(bookingCancelledQueue).to(paymentTopicExchange).with("booking.cancelled");
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

