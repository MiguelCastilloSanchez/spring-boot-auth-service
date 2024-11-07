package com.example.auth_service.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.auth_service.entities.users.User;
import com.example.auth_service.entities.users.dtos.RabbitRegisterDTO;
import com.example.auth_service.entities.users.dtos.RegisterDTO;
import com.example.auth_service.repositories.UserRepository;
import com.example.auth_service.services.rabbitmq.RabbitSenderService;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RabbitSenderService rabbitSenderService;
    
    @SuppressWarnings("rawtypes")
    public ResponseEntity addUser(RegisterDTO data){

        if (
            (this.userRepository.findByEmail(data.email()) != null) ||
            (this.userRepository.findByName(data.name()).isPresent())
        ) return ResponseEntity.badRequest().body("Username or email already used");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User user = new User(data.name(), data.email(), encryptedPassword, data.role());

        this.userRepository.save(user);
        
        String name = data.name();
        String userId = this.userRepository.findByName(name).get().getId();

        this.sendRegisterMessage(userId, name);

        return ResponseEntity.ok().build();

    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity removeUser(String userId){
        try{
            Optional<User> optionalUser = userRepository.findById(userId);
        
            if (optionalUser.isPresent()) {
                userRepository.deleteById(userId);
                System.out.println("Removed User with ID: " + userId);
                return ResponseEntity.status(HttpStatus.OK).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void sendRegisterMessage(String userId, String name){
        RabbitRegisterDTO message = new RabbitRegisterDTO();
        message.setUserId(userId);
        message.setName(name);

        rabbitSenderService.sendMessage(message);
    }
}
