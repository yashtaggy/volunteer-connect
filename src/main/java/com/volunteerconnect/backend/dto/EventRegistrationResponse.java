package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.RegistrationStatus; // Import the new enum
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data // Generates getters, setters, toString, equals, and hashCode
@Builder // Enables the .builder() pattern for easy object creation
public class EventRegistrationResponse {
    private Long id;
    private Long eventId;
    private String eventTitle; // Include event title for better context
    private Long volunteerId;
    private String volunteerUsername; // Include volunteer username for better context
    private LocalDateTime registrationDate;
    private RegistrationStatus status; // Include the registration status
}