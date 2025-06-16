package com.volunteerconnect.backend.dto;

import lombok.Data; // Provides @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.AllArgsConstructor; // ADD THIS
import lombok.Data;              // ADD THIS
import lombok.NoArgsConstructor;   // ADD THIS

@Data // Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}