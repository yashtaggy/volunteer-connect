package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.AuthRequest;
import com.volunteerconnect.backend.dto.JwtResponseDTO;
import com.volunteerconnect.backend.dto.RegisterRequest;
import com.volunteerconnect.backend.dto.UserProfileResponse; // <-- Import this
import com.volunteerconnect.backend.dto.UserProfileUpdateRequest; // <-- Import this

public interface UserService {

    String registerUser(RegisterRequest registerRequest);

    JwtResponseDTO authenticateUser(AuthRequest authRequest);

    // --- NEW METHODS FOR PROFILE MANAGEMENT ---
    UserProfileResponse getUserProfileById(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request);
    // --- END NEW METHODS ---
}