package com.volunteerconnect.backend.dto;

import lombok.Data; // Remember Lombok helps generate getters/setters

@Data // This Lombok annotation generates getters, setters, equals, hashCode, and toString methods
public class LoginRequest {
    private String username;
    private String password;
}