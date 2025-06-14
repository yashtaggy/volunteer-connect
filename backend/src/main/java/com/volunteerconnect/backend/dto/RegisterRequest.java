package com.volunteerconnect.backend.dto;

import lombok.Data; // Provides @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
}