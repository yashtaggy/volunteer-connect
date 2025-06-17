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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
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
                // NEW: If role is provided in request, use it, otherwise default to VOLUNTEER
                .role(registerRequest.getRole() != null ? registerRequest.getRole() : com.volunteerconnect.backend.model.role.Role.VOLUNTEER)
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

            // Get the UserDetails object from the authentication principal
            // It will be your 'User' entity since you implement UserDetails
            User authenticatedUser = (User) authentication.getPrincipal();

            String token = jwtService.generateToken(authenticatedUser.getUsername());

            // NEW: Construct LoginResponse with all required fields using the builder
            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .userId(authenticatedUser.getId())
                    .username(authenticatedUser.getUsername())
                    .email(authenticatedUser.getEmail())
                    .firstName(authenticatedUser.getFirstName())
                    .lastName(authenticatedUser.getLastName())
                    .role(authenticatedUser.getRole())
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            // Log this for debugging
            System.err.println("Bad credentials for user: " + loginRequest.getUsername()); // Replace with actual logger
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        } catch (AuthenticationException e) {
            // Log this for debugging
            System.err.println("Authentication error for user: " + loginRequest.getUsername() + ": " + e.getMessage()); // Replace with actual logger
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // 401
        } catch (Exception e) {
            // Log this for debugging
            System.err.println("An unexpected error occurred during login for user: " + loginRequest.getUsername() + ": " + e.getMessage()); // Replace with actual logger
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // 500
        }
    }
}