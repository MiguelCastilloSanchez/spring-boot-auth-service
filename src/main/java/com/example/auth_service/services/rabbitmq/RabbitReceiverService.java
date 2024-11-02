package com.example.auth_service.services.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.auth_service.services.UserService;


@Service
@RequiredArgsConstructor
public class RabbitReceiverService {

    @Autowired
    private UserService userService;

    @RabbitListener(queues = "${spring.rabbitmq.queue.auth}")
    public void receiveMessage(String userId) {
        userId = userId.replace("\"", "");
        userService.removeUser(userId);
    }
}
