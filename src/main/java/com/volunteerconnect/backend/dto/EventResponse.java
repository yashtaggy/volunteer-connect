package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import com.volunteerconnect.backend.dto.UserSummaryDto;
import com.volunteerconnect.backend.dto.organization.OrganizationSummaryDto; // NEW import for OrganizationSummaryDto

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private int capacity;
    private boolean active;

    private UserSummaryDto organizer;

    // NEW: Organization information as a nested DTO
    private OrganizationSummaryDto organization;

    private List<UserSummaryDto> registeredVolunteers;
}