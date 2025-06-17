package com.volunteerconnect.backend.dto;

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

    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private int capacity;
    private boolean active;

    private Long organizerId;

    // NEW: Organization ID for the request
    private Long organizationId;
}