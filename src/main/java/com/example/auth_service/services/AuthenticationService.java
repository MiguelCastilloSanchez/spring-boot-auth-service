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

    @Autowired
    private UserService userService;

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

                userService.sendRegisterMessage(user.getName());

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
        + "<body style=\"font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f9f9f9;\">"
        + "<div style=\"max-width: 600px; margin: 20px auto; background-color: #ffffff; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1);\">"
        
        + "<div style=\"background-color: #BB86FC; color: #ffffff; padding: 15px 20px; border-radius: 8px 8px 0 0; text-align: center;\">"
        + "<h1 style=\"margin: 0; font-size: 24px;\">Welcome to Music Reviews Site!</h1>"
        + "</div>"
        
        + "<div style=\"padding: 20px;\">"
        + "<p style=\"font-size: 16px; color: #333333;\">Hello,</p>"
        + "<p style=\"font-size: 16px; color: #333333;\">Thank you for registering with <strong>Music Reviews Site</strong>. "
        + "Please use the verification code below to complete your registration:</p>"
        
        + "<div style=\"background-color: #f4f4f4; text-align: center; padding: 15px; margin: 20px 0; border: 1px dashed #BB86FC; border-radius: 5px;\">"
        + "<p style=\"font-size: 22px; font-weight: bold; color: #333333; letter-spacing: 3px; margin: 0;\">"
        + verificationCode
        + "</p>"
        + "</div>"
        
        + "<p style=\"font-size: 14px; color: #666666;\">If you did not request this verification code, please ignore this email. "
        + "For your security, do not share this code with anyone.</p>"
        + "</div>"
        
        + "<div style=\"background-color: #f5f5f5; color: #999999; padding: 10px 20px; border-radius: 0 0 8px 8px; text-align: center; font-size: 12px;\">"
        + "<p style=\"margin: 0;\">&copy; 2024 Music Reviews Site. All rights reserved.</p>"
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
        return LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-06:00"));
    }
}

