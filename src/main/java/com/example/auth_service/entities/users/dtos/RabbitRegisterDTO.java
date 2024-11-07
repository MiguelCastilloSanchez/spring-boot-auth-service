package com.example.auth_service.entities.users.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RabbitRegisterDTO{
    private String userId;
    private String name;
}
