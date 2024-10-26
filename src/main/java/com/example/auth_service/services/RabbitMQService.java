package com.example.auth_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RabbitMQService {
    private final AmqpTemplate amqpTemplate;

    @Value("${spring.rabbitmq.exchange.app}")
    private String EXCHANGE;

    @Value("${spring.rabbitmq.routing.key.user}")
    private String ROUTING_KEY;

    public void sendMessage(String userId) {
        amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, userId);
    }
}
