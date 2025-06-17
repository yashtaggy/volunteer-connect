package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String firstName;
    private String lastName;
    private String email;
    // IMPORTANT: Do NOT include 'username' or 'password' here for security reasons,
    // as they should be updated via separate, specific processes.
    // Do NOT include 'role' here unless you intend to allow admins to use this DTO
    // to change roles, and have separate logic for it in the controller.
}