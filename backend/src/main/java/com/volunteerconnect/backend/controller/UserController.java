package com.volunteerconnect.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // NEW: Import for method-level security
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users") // Matches the protected path in SecurityConfig
public class UserController {

    @GetMapping("/profile") // A specific endpoint under /api/users
    // You could also use @PreAuthorize("hasRole('USER')") if you want to explicitly check the role
    public ResponseEntity<String> getUserProfile() {
        return ResponseEntity.ok("Hello from secured /api/users/profile! You are authenticated as a USER.");
    }

    // Add more user-related endpoints here later (e.g., getUserById, updateUser)
}