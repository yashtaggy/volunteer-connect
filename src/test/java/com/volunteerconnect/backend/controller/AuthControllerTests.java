package com.volunteerconnect.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.volunteerconnect.backend.dto.LoginRequest;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.model.Role;
import com.volunteerconnect.backend.service.AuthService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;


import java.util.Optional;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*; // This covers 'when', 'mock', 'verify' etc.
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTests {

    private MockMvc mockMvc;

    @Mock
    private AuthService userService;

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
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER.name()) // <--- CORRECTED: Convert enum to string
                .build();

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedpassword")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER)
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
        RegisterRequest registerRequest = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .email("existing@example.com")
                .firstName("Existing")
                .lastName("User")
                .role(Role.VOLUNTEER.name()) // <--- CORRECTED: Convert enum to string
                .build();

        User existingUser = User.builder()
                .username("existinguser")
                .password("encodedpassword")
                .email("existing@example.com")
                .firstName("Existing")
                .lastName("User")
                .role(Role.VOLUNTEER)
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

        User authenticatedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(Role.VOLUNTEER)
                .password("encodedPassword")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(authenticatedUser);
        when(authentication.getName()).thenReturn(authenticatedUser.getUsername());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn(jwtToken);


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
                .andExpect(jsonPath("$.role").value(authenticatedUser.getRole().name()));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(any(UserDetails.class));
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
        verify(jwtService, never()).generateToken(any(UserDetails.class));
    }
}