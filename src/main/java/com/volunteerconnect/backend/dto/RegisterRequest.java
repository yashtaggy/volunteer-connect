// Example for RegisterRequest.java
package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.Role; // Import Role
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder; // Often useful for requests too

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Add @Builder if you use .builder() for this DTO
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String role; // Ensure this is present
}