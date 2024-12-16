package com.example.auth_service.entities.users.dtos;

public record VerifyDTO(

    String email,
    String verificationCode

) {
    
}
