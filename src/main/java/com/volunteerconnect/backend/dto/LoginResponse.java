// Example LoginResponse.java
package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer"; // Ensure this field exists and is initialized
    private String username; // Or whatever your third field is
}