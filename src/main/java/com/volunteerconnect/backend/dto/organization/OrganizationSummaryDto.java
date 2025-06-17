package com.volunteerconnect.backend.dto.organization; // New package for organization DTOs

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSummaryDto {
    private Long id;
    private String name;
    private String contactEmail; // Or just name, based on what you want to expose
}