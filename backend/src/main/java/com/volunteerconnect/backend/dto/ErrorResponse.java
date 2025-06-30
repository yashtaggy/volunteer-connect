// src/main/java/com/volunteerconnect/backend/dto/ErrorResponse.java
package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String message;
    // You could add a timestamp or error code if needed
    // private Instant timestamp;
    // private int statusCode;
}