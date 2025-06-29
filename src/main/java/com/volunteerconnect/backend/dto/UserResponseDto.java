package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.Role; // CORRECTED: Import your Role enum from the 'role' subpackage
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Role role; // Assuming you want to expose the role
}