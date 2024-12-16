package com.example.auth_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.auth_service.entities.users.User;
import com.example.auth_service.entities.users.dtos.AuthenticationDTO;
import com.example.auth_service.entities.users.dtos.LoginResponseDTO;
import com.example.auth_service.entities.users.dtos.RegisterDTO;
import com.example.auth_service.entities.users.dtos.VerifyDTO;
import com.example.auth_service.services.AuthenticationService;
import com.example.auth_service.services.TokenService;
import com.example.auth_service.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/auth", produces = {"application/json"})
public class AuthController {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenService tokenService;

    /**
     * Registers a new user.
     *
     * @param data Object containing user registration data
     * @param result Object checking the validation from registration data
     * @return ResponseEntity indicating success or failure of registration
     */
    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity register(@Valid @RequestBody RegisterDTO data, BindingResult result) {

        if (result.hasErrors()) {
            String error = result.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(error);
        }

        return authenticationService.signup(data);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/verify")
    public ResponseEntity verifyUser(@RequestBody VerifyDTO data) {
        return authenticationService.verifyUser(data);
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/resend")
    public ResponseEntity resendVerificationCode(@RequestParam String email) {
        return authenticationService.resendVerificationCode(email);
    }

    /**
     * Authenticates user login.
     *
     * @param data Object containing user credentials
     * @return ResponseEntity containing authentication token
     */
    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity login(@RequestBody AuthenticationDTO data) {
        var credentials = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        
        try {
            var auth = this.authenticationManager.authenticate(credentials);
            var token = tokenService.generateToken((User) auth.getPrincipal());
            
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect username or password");
        }
    }

    /**
     * Logs out the user by invalidating the provided token.
     *
     * @param authorizationHeader the authorization header containing the user's token
     * @return ResponseEntity indicating the success of the logout operation
     */
    @SuppressWarnings("rawtypes")
    @PostMapping(value = "/logout")
    public ResponseEntity logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");

        tokenService.revokeToken(token);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

}

