package com.example.auth_service.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth_service.entities.users.User;
import com.example.auth_service.entities.users.dtos.RegisterDTO;
import com.example.auth_service.repositories.UserRepository;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RabbitMQService rabbitMQService;
    
    @SuppressWarnings("rawtypes")
    public ResponseEntity addUser(RegisterDTO data){
        if (
            (this.userRepository.findByEmail(data.email()) != null) ||
            (this.userRepository.findByName(data.name()).isPresent())
        ) return ResponseEntity.badRequest().body("Username or email already used");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User user = new User(data.name(), data.email(), encryptedPassword, data.role());

        this.userRepository.save(user);
        
        String userId = this.userRepository.findByName(data.name()).get().getId();
        rabbitMQService.sendMessage(userId);

        return ResponseEntity.ok().build();
    }
    
}
