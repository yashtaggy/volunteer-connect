package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventRequest;
import com.volunteerconnect.backend.dto.EventResponse;
import com.volunteerconnect.backend.model.Event; // Ensure Event is imported

import java.util.List;

public interface EventService {
    EventResponse createEvent(EventRequest eventRequest, Long organizerId);
    List<EventResponse> getAllEvents();
    EventResponse getEventById(Long id);
    EventResponse updateEvent(Long id, EventRequest eventRequest, Long currentUserId);
    void deleteEvent(Long id, Long currentUserId);

    // --- NEW: Method for event registration ---
    EventResponse registerForEvent(Long eventId, Long volunteerId);
    // --- END NEW METHOD ---

    // Helper method (might be internal or public depending on needs)
    Event convertToEntity(EventRequest eventRequest, Long organizerId);
    EventResponse convertToDto(Event event);
}