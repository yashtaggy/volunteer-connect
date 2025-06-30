package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // Assuming role is sent as a string (e.g., "VOLUNTEER", "ORGANIZER")
}