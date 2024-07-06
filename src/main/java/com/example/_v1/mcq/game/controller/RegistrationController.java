package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.attributes.User;
import com.example._v1.mcq.game.respository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
public class RegistrationController {

    // Regular expressions for validation
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/api/register")
    public ResponseEntity<String> register(@RequestBody User userDetails) {
        try {
            validateUser(userDetails);
            userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            userRepo.save(userDetails);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User " + userDetails.getUserName() + " successfully registered!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }

    private void validateUser(User userDetails) {
        validateUsername(userDetails.getUserName());
        validatePassword(userDetails.getPassword());
        validateEmail(userDetails.getEmail());
        checkUserExists(userDetails);
    }

    private void validateUsername(String username) {
        if (isNullOrEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty!");
        }
        if (!Pattern.matches(USERNAME_REGEX, username)) {
            throw new IllegalArgumentException("Username must be 3-20 characters long and can only contain letters, numbers, and underscores.");
        }
    }

    private void validatePassword(String password) {
        if (isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password cannot be empty!");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
    }

    private void validateEmail(String email) {
        if (isNullOrEmpty(email)) {
            throw new IllegalArgumentException("Email cannot be empty!");
        }
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    private void checkUserExists(User userDetails) {
        if (userRepo.existsByUserName(userDetails.getUserName())) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepo.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email already exists!");
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}