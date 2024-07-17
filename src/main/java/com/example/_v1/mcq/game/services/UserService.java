package com.example._v1.mcq.game.services;

import com.example._v1.mcq.game.entity.User;
import com.example._v1.mcq.game.respository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public User registerUser(User userDetails) {
        validateUser(userDetails);
        userDetails.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        return userRepo.save(userDetails);
    }
    public User findByUsername(String username) {
        return userRepo.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
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