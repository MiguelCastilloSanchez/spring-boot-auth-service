package com.example.auth_service.services;

import com.example.auth_service.entities.users.User;
import com.example.auth_service.entities.users.UserRole;
import com.example.auth_service.entities.users.dtos.RegisterDTO;
import com.example.auth_service.entities.users.dtos.VerifyDTO;
import com.example.auth_service.repositories.UserRepository;

import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${admin.code}")
    private String adminCode;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email);
    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity signup(RegisterDTO data) {
        if (
            (this.userRepository.findByEmail(data.email()) != null) ||
            (this.userRepository.findByName(data.name()).isPresent())
        ) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username or email already used");

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());

        User user = new User(data.name(), data.email(), encryptedPassword);

        if(data.code() != null && data.code().equals(adminCode)){
            user.setRole(UserRole.ADMIN);
        }else{
            user.setRole(UserRole.USER);
        }
        
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(generateExpirationDate());
        user.setEnabled(false);
        sendVerificationEmail(user);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Verification code sent to your email");
    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity verifyUser(VerifyDTO data) {
        UserDetails userDetails = userRepository.findByEmail(data.email());
        Optional<User> optionalUser = userRepository.findByName(userDetails.getUsername());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now().toInstant(ZoneOffset.of("-06:00")))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation code expired");
            }
            if (user.getVerificationCode().equals(data.verificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Wrong validation code");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body("User verified!");
    }

    @SuppressWarnings("rawtypes")
    public ResponseEntity resendVerificationCode(String email) {
        UserDetails userDetails = userRepository.findByEmail(email);
        Optional<User> optionalUser = userRepository.findByName(userDetails.getUsername());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(generateExpirationDate());
            sendVerificationEmail(user);
            userRepository.save(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).body("Code resent");
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to Music Reviews Site!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #BB86FC;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            // Handle email sending exception
            e.printStackTrace();
        }
    }
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusMinutes(1).toInstant(ZoneOffset.of("-06:00"));
    }
}

