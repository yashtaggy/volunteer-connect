package com.volunteerconnect.backend.dto;

import com.volunteerconnect.backend.model.RegistrationStatus; // Import the enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationResponse {

    private Long id;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;

    // Nested DTOs for Event and Volunteer summaries
    private EventSummaryDto event; // A new DTO we'll create next
    private UserSummaryDto volunteer; // Reuse the UserSummaryDto for the volunteer
}