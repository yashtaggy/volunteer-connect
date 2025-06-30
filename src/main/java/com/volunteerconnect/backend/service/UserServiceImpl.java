package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.AuthRequest;
import com.volunteerconnect.backend.dto.JwtResponseDTO;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.dto.UserProfileResponse;
import com.volunteerconnect.backend.dto.UserProfileUpdateRequest;
import com.volunteerconnect.backend.exception.ResourceNotFoundException;
import com.volunteerconnect.backend.model.Role; // <-- Import Role enum
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.UserRepository;
import com.volunteerconnect.backend.security.JwtService;
import com.volunteerconnect.backend.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public String registerUser(RegisterRequest registerRequest) {
        // Check if username or email already exists to prevent duplicates
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists!");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists!");
        }

        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                // --- FIX 1: Convert String role to Role enum ---
                .role(Role.valueOf(registerRequest.getRole().toUpperCase())) // Ensure role string matches enum names
                // --- END FIX 1 ---
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();
        userRepository.save(newUser);
        log.info("User registered successfully: {}", newUser.getUsername());
        return "User Registered Successfully";
    }

    @Override
    public JwtResponseDTO authenticateUser(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + authRequest.getUsername()));

            // --- FIX 2: Pass User object to generateToken ---
            String token = jwtService.generateToken(user); // Now passing the User object directly
            // --- END FIX 2 ---

            return JwtResponseDTO.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name()) // Return role as string in DTO
                    .build();
        } else {
            throw new UsernameNotFoundException("Invalid user credentials!");
        }
    }

    @Override
    public UserProfileResponse getUserProfileById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Update fields that are allowed to be changed
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdatedDate(LocalDateTime.now()); // Update timestamp

        // Optionally, check if the new email already exists for another user
        // This check ensures unique emails across users
        if (userRepository.findByEmail(request.getEmail()).isPresent() &&
                !user.getEmail().equals(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists for another user!");
        }

        User updatedUser = userRepository.save(user); // Save the updated user

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .role(updatedUser.getRole())
                .build();
    }
}