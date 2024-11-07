package com.example.auth_service.services.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.auth_service.entities.users.dtos.RabbitRegisterDTO;


@Service
@RequiredArgsConstructor
public class RabbitSenderService {

    private final AmqpTemplate amqpTemplate;

    @Value("${spring.rabbitmq.exchange.app}")
    private String EXCHANGE;

    @Value("${spring.rabbitmq.routing.key.user}")
    private String ROUTING_KEY;

    public void sendMessage(RabbitRegisterDTO message) {
        amqpTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
    }

}