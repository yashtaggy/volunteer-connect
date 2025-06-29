package com.volunteerconnect.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {
    // Username is typically not updated via profile update, but via separate process
    // Role is typically not updated by user themselves, only by admin

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String firstName;
    private String lastName;

    // You might add password change fields here, but it's often a separate endpoint
    // private String currentPassword;
    // private String newPassword;
}