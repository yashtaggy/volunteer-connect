package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.EventRequest;
import com.volunteerconnect.backend.dto.EventResponse;
import com.volunteerconnect.backend.model.User; // Ensure User model is imported for getPrincipal()
import com.volunteerconnect.backend.service.EventService; // Import the NEW EventService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    // Inject the NEW EventService interface
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // Helper method to get the current authenticated user's ID
    // This assumes your Authentication.getPrincipal() returns your User entity directly
    // from your UserDetailsService/JwtService implementation.
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        // Ensure the principal is an instance of your User model
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        // This fallback should rarely be hit if your security configuration is correct
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Could not determine authenticated user ID from principal.");
    }

    // --- CREATE Operation ---
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')") // Only ORGANIZER can create events
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest) {
        // The current authenticated user (who is an ORGANIZER) will be the event's organizer
        Long currentUserId = getCurrentAuthenticatedUserId();
        EventResponse newEvent = eventService.createEvent(eventRequest, currentUserId);
        return new ResponseEntity<>(newEvent, HttpStatus.CREATED);
    }

    // --- READ All Events ---
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can view all events
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> eventResponses = eventService.getAllEvents();
        return ResponseEntity.ok(eventResponses);
    }

    // --- READ Single Event by ID ---
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can view an event by ID
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    // --- UPDATE Operation ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')") // Only Organizers or Admins can update events
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest eventRequest) {
        Long currentUserId = getCurrentAuthenticatedUserId(); // Needed for authorization check in service
        EventResponse updatedEvent = eventService.updateEvent(id, eventRequest, currentUserId);
        return ResponseEntity.ok(updatedEvent);
    }

    // --- DELETE Operation ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')") // Only Organizers or Admins can delete events
    public ResponseEntity<HttpStatus> deleteEvent(@PathVariable Long id) {
        Long currentUserId = getCurrentAuthenticatedUserId(); // Needed for authorization check in service
        eventService.deleteEvent(id, currentUserId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}