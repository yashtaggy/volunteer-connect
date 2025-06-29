package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.Role; // Import Role enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role; // Send the Role enum directly
    // Add other fields you want to expose in the profile
}