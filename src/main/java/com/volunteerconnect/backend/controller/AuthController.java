package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.LoginRequest;
import com.volunteerconnect.backend.dto.LoginResponse;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.service.UserService;
import com.volunteerconnect.backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// Import for specific authentication exceptions
import org.springframework.security.core.AuthenticationException; // NEW IMPORT
import org.springframework.security.authentication.BadCredentialsException; // NEW IMPORT (if you didn't have it)
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();

        if (userService.findByUsername(newUser.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        userService.registerNewUser(newUser);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtService.generateToken(loginRequest.getUsername());

            return new ResponseEntity<>(new LoginResponse(
                    token,
                    "Bearer",
                    loginRequest.getUsername()
            ), HttpStatus.OK);
        } catch (BadCredentialsException e) { // Catch specifically for incorrect credentials
            // Consider logging e.g., log.warn("Bad credentials for user: {}", loginRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        } catch (AuthenticationException e) { // Catch any other authentication-related issues
            // Consider logging e.g., log.error("Authentication error for user: {}", loginRequest.getUsername(), e);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        } catch (Exception e) { // Generic catch-all for any other unexpected errors
            // Consider logging e.g., log.error("An unexpected error occurred during login for user: {}", loginRequest.getUsername(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }
}