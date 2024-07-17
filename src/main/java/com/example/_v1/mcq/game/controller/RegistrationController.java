package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.entity.User;
import com.example._v1.mcq.game.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class RegistrationController {

    private final UserService userService;

    @PostMapping("/api/register")
    public ResponseEntity<String> register(@RequestBody User userDetails) {
        try {
            User registeredUser = userService.registerUser(userDetails);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User " + registeredUser.getUserName() + " successfully registered!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred. Please try again later.");
        }
    }
}