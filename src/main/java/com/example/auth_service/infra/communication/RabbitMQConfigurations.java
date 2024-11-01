package com.example.auth_service.infra.communication;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfigurations {

    @Value("${spring.rabbitmq.queue.auth}")
    private String AUTH_QUEUE;

    @Value("${spring.rabbitmq.queue.user}")
    private String USER_QUEUE;

    @Value("${spring.rabbitmq.exchange.app}")
    private String EXCHANGE;

    @Value("${spring.rabbitmq.routing.key.user}")
    private String ROUTING_KEY;

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE, true);
    }

    @Bean
    public Queue authQueue() {
        return new Queue(AUTH_QUEUE, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Binding bindingUserQueue(Queue userQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public Binding bindingAuthQueue(Queue authQueue, TopicExchange exchange) {
        return BindingBuilder.bind(authQueue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}