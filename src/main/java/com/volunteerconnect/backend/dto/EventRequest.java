package com.volunteerconnect.backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Event date and time are required")
    @FutureOrPresent(message = "Event date and time must be in the present or future")
    private LocalDateTime eventDate;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private boolean active = true; // Default to true if not provided

    private String requiredSkills; // This field was missing, added to match frontend form and Event model

    private Long organizerId; // This is typically derived from the authenticated user for creation

    @NotNull(message = "Organization ID is required") // Event.organization is nullable = false
    private Long organizationId;
}