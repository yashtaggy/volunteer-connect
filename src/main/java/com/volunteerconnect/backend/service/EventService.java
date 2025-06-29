package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventRequest;  // Use EventRequest
import com.volunteerconnect.backend.dto.EventResponse; // Use EventResponse
import java.util.List;

public interface EventService {
    List<EventResponse> getAllEvents();
    EventResponse getEventById(Long eventId);
    EventResponse createEvent(EventRequest eventRequest, Long currentUserId); // currentUserId is the organizer
    EventResponse updateEvent(Long eventId, EventRequest eventRequest, Long currentUserId); // currentUserId for authorization
    void deleteEvent(Long eventId, Long currentUserId); // currentUserId for authorization
}