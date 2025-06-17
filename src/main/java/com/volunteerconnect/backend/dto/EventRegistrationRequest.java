package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// You might add validation annotations here later if needed
// import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    // @NotNull(message = "Event ID is required") // Example validation
    private Long eventId;

    // @NotNull(message = "Volunteer ID is required") // Example validation
    private Long volunteerId;

    // Status is typically managed by the server on creation (e.g., defaults to PENDING)
    // but could be included here if clients can set an initial status.
    // For simplicity, we'll set it in the controller for now.
}