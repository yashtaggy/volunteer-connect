package com.volunteerconnect.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerconnect.backend.dto.LoginRequest;
import com.volunteerconnect.backend.dto.LoginResponse;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.model.User; // Added this import
import com.volunteerconnect.backend.model.role.Role; // NEW: Import Role enum for tests
import com.volunteerconnect.backend.service.UserService;
import com.volunteerconnect.backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTests {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerUserSuccess() throws Exception {
        // Corrected instantiation using builder pattern for RegisterRequest
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER) // Explicitly set role for the test
                .build();

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedpassword") // Password would be encoded by service
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER) // Match the role
                .build();

        when(userService.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userService.registerNewUser(any(User.class))).thenReturn(savedUser); // Mock to return the savedUser

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully!"));

        verify(userService, times(1)).findByUsername(registerRequest.getUsername());
        verify(userService, times(1)).registerNewUser(any(User.class));
    }

    @Test
    void registerUserExists() throws Exception {
        // Corrected instantiation using builder pattern for RegisterRequest
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .email("existing@example.com")
                .firstName("Existing")
                .lastName("User")
                .role(Role.VOLUNTEER) // Explicitly set role for the test
                .build();

        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedpassword")
                .email("existing@example.com")
                .firstName("Existing")
                .lastName("User")
                .role(Role.VOLUNTEER) // Match the role
                .build();

        when(userService.findByUsername(any(String.class))).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken!"));

        verify(userService, times(1)).findByUsername(registerRequest.getUsername());
        verify(userService, never()).registerNewUser(any(User.class));
    }

    @Test
    void loginUserSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        String jwtToken = "mocked_jwt_token";

        // Mock User object that implements UserDetails and has all fields expected by LoginResponse
        User authenticatedUser = User.builder()
                .id(1L) // Assuming ID is 1L for the test
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER) // Ensure the role is set for the mock user
                .password("encodedPassword") // This isn't strictly needed for the principal mock, but good to have a complete mock
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        // Important: authentication.getPrincipal() should return your User object
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(authentication.getName()).thenReturn(authenticatedUser.getUsername()); // Or testuser directly


        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(String.class))).thenReturn(jwtToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken))
                .andExpect(jsonPath("$.userId").value(authenticatedUser.getId()))
                .andExpect(jsonPath("$.username").value(authenticatedUser.getUsername()))
                .andExpect(jsonPath("$.email").value(authenticatedUser.getEmail()))
                .andExpect(jsonPath("$.firstName").value(authenticatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(authenticatedUser.getLastName()))
                .andExpect(jsonPath("$.role").value(authenticatedUser.getRole().name())); // Check role by its string name

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(loginRequest.getUsername());
    }

    @Test
    void loginUserFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest("wronguser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateToken(anyString());
    }
}