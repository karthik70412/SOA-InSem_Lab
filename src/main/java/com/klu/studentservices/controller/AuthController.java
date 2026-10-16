package com.klu.studentservices.controller;

import com.klu.studentservices.dto.LoginRequest;
import com.klu.studentservices.dto.RegisterRequest;
import com.klu.studentservices.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        Map<String, String> response = new HashMap<>();

        response.put("message", message);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request) {

        String message = authService.login(request);

        Map<String, String> response = new HashMap<>();

        response.put("message", message);

        return ResponseEntity.ok(response);
    }
}