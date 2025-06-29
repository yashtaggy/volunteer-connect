package com.volunteerconnect.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull; // <--- This import is now crucial

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    @NotNull(message = "Event ID is required") // <--- Uncommented and made active
    private Long eventId;

}