package com.example.auth_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class RabbitMQService {
    private final AmqpTemplate amqpTemplate;

    private final String EXCHANGE = "app-exchange";

    private final String ROUTING_KEY = "user.register";

    public void sendMessage(String userId) {
        amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, userId);
    }
}
