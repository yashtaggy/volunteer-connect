package com.volunteerconnect.backend.dto.organization; // New package for organization DTOs

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationResponse {
    private Long id;
    private String name;
    private String description;
    private String contactEmail;
    private String phoneNumber;
    private String websiteUrl;
    private String address;
    private boolean active;
}