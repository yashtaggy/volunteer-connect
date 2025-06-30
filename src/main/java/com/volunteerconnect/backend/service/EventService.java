package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventCreateRequest; // <-- Import this
import com.volunteerconnect.backend.dto.EventResponse; // <-- Assuming you have/will have an EventResponse DTO

import java.util.List;

public interface EventService {

    List<EventResponse> getAllEvents(); // Assuming this method already exists for listing events

    // --- NEW METHOD FOR EVENT CREATION ---
    EventResponse createEvent(EventCreateRequest request, Long organizerId);
    // --- END NEW METHOD ---
}