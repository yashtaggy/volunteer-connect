package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.EventRegistrationRequest;
import com.volunteerconnect.backend.dto.EventRegistrationResponse;
import com.volunteerconnect.backend.model.User; // Import your User model for principal casting
import com.volunteerconnect.backend.service.EventRegistrationService;
import jakarta.validation.Valid; // For @Valid annotation
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
@RequestMapping("/api/event-registrations") // Base path for registration endpoints
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    @Autowired
    public EventRegistrationController(EventRegistrationService eventRegistrationService) {
        this.eventRegistrationService = eventRegistrationService;
    }

    // Helper method to get the current authenticated user's ID
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        if (authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Could not determine authenticated user ID from principal.");
    }

    /**
     * Registers the authenticated VOLUNTEER for an event.
     * Endpoint: POST /api/event-registrations
     * Request Body: { "eventId": 123 }
     */
    @PostMapping
    @PreAuthorize("hasRole('VOLUNTEER')") // Only volunteers can register for events
    public ResponseEntity<EventRegistrationResponse> registerForEvent(@Valid @RequestBody EventRegistrationRequest request) {
        Long volunteerId = getCurrentAuthenticatedUserId();
        EventRegistrationResponse response = eventRegistrationService.registerForEvent(request.getEventId(), volunteerId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Unregisters the authenticated VOLUNTEER from an event.
     * Endpoint: DELETE /api/event-registrations/{eventId}
     * Note: We are using path variable for eventId and getting volunteerId from token.
     * This simplifies the DELETE request from the client.
     */
    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('VOLUNTEER')") // Only volunteers can unregister from events they are registered for
    public ResponseEntity<Void> unregisterFromEvent(@PathVariable Long eventId) {
        Long volunteerId = getCurrentAuthenticatedUserId();
        eventRegistrationService.unregisterFromEvent(eventId, volunteerId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
    }

    /**
     * Get all registrations for a specific event.
     * Endpoint: GET /api/event-registrations/event/{eventId}
     * Access: Organizers or Admins can view registrations for an event.
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<List<EventRegistrationResponse>> getRegistrationsForEvent(@PathVariable Long eventId) {
        List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsForEvent(eventId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Get all events a specific volunteer has registered for.
     * Endpoint: GET /api/event-registrations/volunteer/{volunteerId}
     * Access: An authenticated user can view their own registrations. Admin can view anyone's.
     * If fetching for others, ensure currentUserId matches volunteerId or current user is ADMIN.
     */
    @GetMapping("/volunteer/{volunteerId}")
    @PreAuthorize("hasAnyRole('ADMIN') or (#volunteerId == authentication.principal.id)") // Admin or owner
    public ResponseEntity<List<EventRegistrationResponse>> getRegistrationsByVolunteer(@PathVariable Long volunteerId) {
        List<EventRegistrationResponse> registrations = eventRegistrationService.getRegistrationsByVolunteer(volunteerId);
        return ResponseEntity.ok(registrations);
    }

    /**
     * Get a single registration by its ID.
     * Endpoint: GET /api/event-registrations/{registrationId}
     * Access: Authenticated users who are related to the registration (volunteer, organizer of event, or admin).
     * For simplicity, this example just uses isAuthenticated(), but you might want more granular check in service.
     */
    @GetMapping("/{registrationId}")
    @PreAuthorize("isAuthenticated()") // Or add more specific checks based on roles/ownership
    public ResponseEntity<EventRegistrationResponse> getRegistrationById(@PathVariable Long registrationId) {
        EventRegistrationResponse registration = eventRegistrationService.getRegistrationById(registrationId);
        // Optional: Add a check here in controller or service if the current user is authorized to view this specific registration.
        // For example: If (registration.getVolunteerId() != getCurrentAuthenticatedUserId() && !hasRole('ADMIN')) throw FORBIDDEN.
        return ResponseEntity.ok(registration);
    }
}