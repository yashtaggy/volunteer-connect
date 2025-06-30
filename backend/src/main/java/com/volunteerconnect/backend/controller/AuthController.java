package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.LoginRequest;
import com.volunteerconnect.backend.dto.LoginResponse;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.model.Role;
import com.volunteerconnect.backend.service.AuthService;
import com.volunteerconnect.backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        if (userService.findByUsername(registerRequest.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        Role roleToAssign = Role.VOLUNTEER;
        String inputRole = registerRequest.getRole();

        if (inputRole != null) {
            try {
                roleToAssign = Role.valueOf(inputRole.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role specified during registration, defaulting to VOLUNTEER: " + inputRole);
            }
        }

        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .password(registerRequest.getPassword())
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .role(roleToAssign)
                .build();

        userService.registerNewUser(newUser);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get the authenticated UserDetails object
            // It will be an instance of your 'User' model because your UserDetailsService returns it.
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // MODIFIED LINE: Pass the UserDetails object (your 'User' instance) to generateToken
            String token = jwtService.generateToken(userDetails);

            // Cast back to your User model if you need user-specific fields for the response
            User authenticatedUser = (User) userDetails;

            LoginResponse response = LoginResponse.builder()
                    .token(token)
                    .userId(authenticatedUser.getId())
                    .username(authenticatedUser.getUsername())
                    .email(authenticatedUser.getEmail())
                    .firstName(authenticatedUser.getFirstName())
                    .lastName(authenticatedUser.getLastName())
                    .role(authenticatedUser.getRole().name()) // Convert enum to string
                    .build();

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (BadCredentialsException e) {
            System.err.println("Bad credentials for user: " + loginRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (AuthenticationException e) {
            System.err.println("Authentication error for user: " + loginRequest.getUsername() + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            System.err.println("Unexpected error during login: " + loginRequest.getUsername() + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}