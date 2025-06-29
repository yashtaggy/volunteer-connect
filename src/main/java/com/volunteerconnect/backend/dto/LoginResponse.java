// Example for LoginResponse.java
package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
}