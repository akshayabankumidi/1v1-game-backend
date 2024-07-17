package com.example._v1.mcq.game.controller;

import com.example._v1.mcq.game.DTO.LoginRequest;
import com.example._v1.mcq.game.services.AuthService;
import com.example._v1.mcq.game.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class LoginController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/api/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        if (authService.authenticateUser(loginRequest.getUserName(), loginRequest.getPassword())) {
            return ResponseEntity.ok(jwtUtil.generateToken(loginRequest.getUserName()));
        } else {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }
    }
}