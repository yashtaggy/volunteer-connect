package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.EventRequest;
import com.volunteerconnect.backend.dto.EventResponse;
import com.volunteerconnect.backend.exception.ResourceNotFoundException;
import com.volunteerconnect.backend.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.security.core.Authentication; // Import Authentication
import org.springframework.security.core.context.SecurityContextHolder; // Import SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // Import Map for error responses
import com.volunteerconnect.backend.security.CustomUserDetails;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest) {
        // Retrieve current authenticated user's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long organizerId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            // Assuming your UserDetails implementation has a way to get the ID,
            // or you retrieve it from your UserRepository based on username.
            // For simplicity, let's get the username and find the user.
            // A more robust solution might pass the entire User entity from SecurityContext.
            // For now, let's assume `authentication.getName()` gives the username
            // and you can get the user ID from the UserDetailsService or an additional service.
            // Placeholder: In a real app, you'd fetch the actual User ID associated with this username.
            // For now, we rely on the `organizerId` being passed to the service from a context/DB lookup.
            // The service method expects the ID directly.
            // Let's modify the createEvent endpoint to take the organizer ID from principal directly.
            // If your UserDetails is a custom User object with an ID, you can cast it.
            // If not, you need a way to get the User object by username.
            // Assuming your `CustomUserDetails` (if you have one) implements `UserDetails` and has a `getId()` method.
            // Or you'd fetch it from the database based on authentication.getName() (username).
            // For now, we'll keep it simple and assume we're getting the ID from the principal.
            // Or, more correctly, we'll get it in the service from the Authentication object itself if needed.
            // For this method, let's assume the organizer ID is retrieved within the service layer itself
            // or passed implicitly if your framework handles it.
            // Let's refine this to explicitly get the user ID from authentication for safety.
        }

        // To get the User ID from the authenticated principal:
        // This requires your UserDetails object to store the ID or be your custom User entity.
        // Assuming your custom UserDetails can provide the ID directly.
        Long currentUserId = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.volunteerconnect.backend.security.CustomUserDetails customUserDetails) {
            currentUserId = customUserDetails.getId(); // Assuming CustomUserDetails has a getId()
        } else if (authentication.getName() != null) {
            // Fallback: If CustomUserDetails is not used or doesn't expose ID,
            // you'd typically have a UserService to find user by username.
            // For now, let's assume CustomUserDetails is providing the ID.
            throw new IllegalStateException("Authenticated principal does not contain user ID.");
        }

        if (currentUserId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Should not happen with @PreAuthorize
        }

        EventResponse createdEvent = eventService.createEvent(eventRequest, currentUserId);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()") // Anyone logged in can view all events
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Anyone logged in can view event details
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest eventRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.volunteerconnect.backend.security.CustomUserDetails customUserDetails) {
            currentUserId = customUserDetails.getId();
        } else {
            throw new IllegalStateException("Authenticated principal does not contain user ID.");
        }
        EventResponse updatedEvent = eventService.updateEvent(id, eventRequest, currentUserId);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.volunteerconnect.backend.security.CustomUserDetails customUserDetails) {
            currentUserId = customUserDetails.getId();
        } else {
            throw new IllegalStateException("Authenticated principal does not contain user ID.");
        }
        eventService.deleteEvent(id, currentUserId);
        return ResponseEntity.noContent().build();
    }


    // --- NEW: Endpoint for event registration ---
    @PostMapping("/{id}/register")
    @PreAuthorize("hasRole('VOLUNTEER')") // Only users with 'VOLUNTEER' role can register
    public ResponseEntity<?> registerForEvent(@PathVariable("id") Long eventId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long volunteerId = null;

        // Retrieve the volunteer's ID from the authenticated principal
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.volunteerconnect.backend.security.CustomUserDetails customUserDetails) {
            volunteerId = customUserDetails.getId();
        } else {
            // This case should ideally not be reached if authentication is successful and CustomUserDetails is used.
            // Add robust error handling or ensure your UserDetails implementation always provides the ID.
            return new ResponseEntity<>(Map.of("message", "Could not retrieve volunteer ID from authentication."), HttpStatus.UNAUTHORIZED);
        }

        try {
            EventResponse updatedEvent = eventService.registerForEvent(eventId, volunteerId);
            return new ResponseEntity<>(updatedEvent, HttpStatus.OK); // Or HttpStatus.CREATED if you prefer
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(Map.of("message", e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            return new ResponseEntity<>(Map.of("message", "An unexpected error occurred: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // --- END NEW ENDPOINT ---
}