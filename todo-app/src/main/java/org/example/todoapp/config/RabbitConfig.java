package org.example.todoapp.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/* @EnableRabbit
@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "email-queue";
    public static final String EXCHANGE_NAME = "email-exchange";
    public static final String ROUTING_KEY = "email.routing";

    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE_NAME, true);
    }
    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {

        return BindingBuilder
                .bind(emailQueue)
                .to(emailExchange)
                .with(ROUTING_KEY);
    }

}*/



