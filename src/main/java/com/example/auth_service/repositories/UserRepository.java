package com.example.auth_service.repositories;

import com.example.auth_service.entities.users.User;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    UserDetails findByEmail(String email);
    Optional<User> findByName(String name);
}
