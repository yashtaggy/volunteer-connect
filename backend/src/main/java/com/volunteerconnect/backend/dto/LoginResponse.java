package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor; // Generates a constructor with all arguments
import lombok.Data;              // Generates getters, setters, etc.
import lombok.NoArgsConstructor;   // Generates a no-argument constructor

@Data
@AllArgsConstructor // When you create a LoginResponse object, you can pass token and username directly: new LoginResponse("abc", "user")
@NoArgsConstructor  // Spring/Jackson needs this for deserialization, though not strictly needed here for sending
public class LoginResponse {
    private String token;    // This will hold the JWT (the long, generated string)
    private String username; // We'll send the username back as well
}