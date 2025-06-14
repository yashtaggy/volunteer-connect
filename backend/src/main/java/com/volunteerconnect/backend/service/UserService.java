package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.LoginRequest; // NEW: Import LoginRequest DTO
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.entity.User;
import com.volunteerconnect.backend.repository.UserRepository;
import com.volunteerconnect.backend.security.JwtService; // NEW: Import JwtService
import org.springframework.security.authentication.AuthenticationManager; // NEW: Import AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // NEW: Import UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication; // NEW: Import Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException; // NEW: Import UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService; // NEW: Field for JwtService
    private final AuthenticationManager authenticationManager; // NEW: Field for AuthenticationManager

    // Updated Constructor for dependency injection
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) { // NEW PARAMETERS
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService; // Initialize new fields
        this.authenticationManager = authenticationManager; // Initialize new fields
    }

    public User registerNewUser(RegisterRequest registerRequest) {
        // Check if username or email already exists
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already taken.");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered.");
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Encode password before saving
        user.setEmail(registerRequest.getEmail());
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        return userRepository.save(user);
    }

    // Method to load user by username (used by Spring Security's UserDetailsService)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // NEW METHOD: Authenticates user and generates JWT
    public String authenticateUserAndGenerateToken(LoginRequest loginRequest) {
        // This line is the core of authenticating via Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // If authentication is successful (no exception thrown)
        if (authentication.isAuthenticated()) {
            // Generate and return a JWT using our JwtService
            return jwtService.generateToken(loginRequest.getUsername());
        } else {
            // This 'else' block is mostly a fallback, as authenticate() usually throws directly on failure
            throw new UsernameNotFoundException("Invalid credentials!"); // Or BadCredentialsException
        }
    }
}