package com.volunteerconnect.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerconnect.backend.dto.LoginRequest;
import com.volunteerconnect.backend.dto.LoginResponse;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.service.UserService;
import com.volunteerconnect.backend.model.User; // Added this import

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
        // <<< IMPORTANT CHANGE HERE (corresponds to line 58 in your error) >>>
        // Instantiate RegisterRequest with all 5 arguments as per your DTO's @AllArgsConstructor
        RegisterRequest registerRequest = new RegisterRequest("testuser", "password123", "test@example.com", "Test", "User");

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedpassword") // Password would be encoded by service
                .build();

        when(userService.findByUsername(any(String.class))).thenReturn(Optional.empty());
        when(userService.registerNewUser(any(User.class))).thenReturn(savedUser);

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
        // <<< IMPORTANT CHANGE HERE (corresponds to line 85 in your error) >>>
        // Instantiate RegisterRequest with all 5 arguments as per your DTO's @AllArgsConstructor
        RegisterRequest registerRequest = new RegisterRequest("existinguser", "password123", "existing@example.com", "Existing", "User");

        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedpassword")
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
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String jwtToken = "mocked_jwt_token";
        when(jwtService.generateToken(any(String.class))).thenReturn(jwtToken);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.username").value("testuser"));

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
        verify(jwtService, never()).generateToken(any(String.class));
    }
}