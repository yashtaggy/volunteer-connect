package com.volunteerconnect.backend.dto;

import lombok.Data; // Remember Lombok helps generate getters/setters
import lombok.AllArgsConstructor; // ADD THIS
import lombok.Data;              // ADD THIS
import lombok.NoArgsConstructor;   // ADD THIS


@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
public class LoginRequest {
    private String username;
    private String password;
}