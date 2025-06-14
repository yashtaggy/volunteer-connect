package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.LoginRequest;    // NEW: Import LoginRequest
import com.volunteerconnect.backend.dto.LoginResponse;   // NEW: Import LoginResponse
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.entity.User;
import com.volunteerconnect.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // NEW: Import for login failure
import org.springframework.security.core.userdetails.UsernameNotFoundException; // NEW: Import for login failure
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base path for authentication endpoints
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint for user registration
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User registeredUser = userService.registerNewUser(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Username: " + registeredUser.getUsername());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected errors during registration
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during registration: " + e.getMessage());
        }
    }

    // NEW ENDPOINT: User login
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Call UserService to authenticate and get the JWT token
            String token = userService.authenticateUserAndGenerateToken(loginRequest);

            // Create a LoginResponse object to send back token and username
            LoginResponse response = new LoginResponse(token, loginRequest.getUsername());

            // Return 200 OK with the token in the response body
            return ResponseEntity.ok(response);

        } catch (UsernameNotFoundException e) {
            // Handle case where username is not found
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        } catch (BadCredentialsException e) {
            // Handle case where password is incorrect
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        } catch (Exception e) {
            // Catch any other unexpected errors during login
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login: " + e.getMessage());
        }
    }
}