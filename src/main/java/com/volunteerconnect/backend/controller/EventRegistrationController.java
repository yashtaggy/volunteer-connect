package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.EventRegistrationRequest;
import com.volunteerconnect.backend.dto.EventRegistrationResponse;
import com.volunteerconnect.backend.dto.EventSummaryDto; // For nested event info
import com.volunteerconnect.backend.dto.UserSummaryDto;   // For nested volunteer info
import com.volunteerconnect.backend.model.Event;
import com.volunteerconnect.backend.model.EventRegistration;
import com.volunteerconnect.backend.model.RegistrationStatus;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.EventRepository;
import com.volunteerconnect.backend.repository.EventRegistrationRepository;
import com.volunteerconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // NEW import
import org.springframework.security.core.context.SecurityContextHolder; // NEW import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registrations") // Matches your current code
public class EventRegistrationController {

    @Autowired
    private EventRegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper method to get the current authenticated user's ID
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated or principal not a User instance.");
    }

    // Custom security method for @PreAuthorize to check if the current user owns the registration
    // This method needs to be public for @PreAuthorize to access it.
    public boolean isRegistrationOwner(Long registrationId) {
        Optional<EventRegistration> registration = registrationRepository.findById(registrationId);
        if (registration.isPresent()) {
            return registration.get().getVolunteer().getId().equals(getCurrentAuthenticatedUserId());
        }
        return false; // Registration not found, or not owned by current user
    }

    // Helper method to convert EventRegistration entity to EventRegistrationResponse DTO
    private EventRegistrationResponse convertToDto(EventRegistration registration) {
        EventSummaryDto eventSummaryDto = EventSummaryDto.builder()
                .id(registration.getEvent().getId())
                .title(registration.getEvent().getTitle())
                .eventDate(registration.getEvent().getEventDate())
                .location(registration.getEvent().getLocation())
                .build();

        UserSummaryDto volunteerSummaryDto = UserSummaryDto.builder()
                .id(registration.getVolunteer().getId())
                .username(registration.getVolunteer().getUsername())
                .email(registration.getVolunteer().getEmail())
                .firstName(registration.getVolunteer().getFirstName())
                .lastName(registration.getVolunteer().getLastName())
                .build();

        return EventRegistrationResponse.builder()
                .id(registration.getId())
                .registrationDate(registration.getRegistrationDate())
                .status(registration.getStatus())
                .event(eventSummaryDto)
                .volunteer(volunteerSummaryDto)
                .build();
    }

    // --- CREATE Registration ---
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Any authenticated user can register for an event
    public ResponseEntity<EventRegistrationResponse> createRegistration(@RequestBody EventRegistrationRequest request) {
        // Enforce that a user can only register for themselves
        if (!getCurrentAuthenticatedUserId().equals(request.getVolunteerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only register for yourself.");
        }

        User volunteer = userRepository.findById(request.getVolunteerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer with ID " + request.getVolunteerId() + " not found."));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + request.getEventId() + " not found."));

        // Basic check for capacity
        if (event.getRegistrations().size() >= event.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event capacity reached.");
        }

        // Check if volunteer is already registered for this event
        boolean alreadyRegistered = event.getRegistrations().stream()
                .anyMatch(reg -> reg.getVolunteer().getId().equals(volunteer.getId()));
        if (alreadyRegistered) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Volunteer is already registered for this event.");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .volunteer(volunteer)
                .registrationDate(LocalDateTime.now()) // Set current date/time
                .status(RegistrationStatus.PENDING)    // Default status on creation
                .build();

        EventRegistration savedRegistration = registrationRepository.save(registration);
        return new ResponseEntity<>(convertToDto(savedRegistration), HttpStatus.CREATED);
    }

    // --- READ All Registrations for a Specific Event ---
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')") // Only Organizers or Admins can view registrations for a specific event
    public ResponseEntity<List<EventRegistrationResponse>> getRegistrationsByEvent(@PathVariable Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));

        // Efficiently fetch registrations for the event (assuming findByEvent method in repo)
        // If you don't have findByEvent in repository, update your repository
        // public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
        //     List<EventRegistration> findByEvent(Event event);
        // }
        List<EventRegistration> registrations = registrationRepository.findByEvent(event);


        List<EventRegistrationResponse> responses = registrations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // --- READ All Registrations by a Specific Volunteer ---
    @GetMapping("/volunteer/{volunteerId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN') or #volunteerId == authentication.principal.id")
    public ResponseEntity<List<EventRegistrationResponse>> getRegistrationsByVolunteer(@PathVariable Long volunteerId) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer with ID " + volunteerId + " not found."));

        // Efficiently fetch registrations for the volunteer (assuming findByVolunteer method in repo)
        // If you don't have findByVolunteer in repository, update your repository
        // public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
        //     List<EventRegistration> findByVolunteer(User volunteer);
        // }
        List<EventRegistration> registrations = registrationRepository.findByVolunteer(volunteer);


        List<EventRegistrationResponse> responses = registrations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    // --- READ Single Registration by ID ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN') or @eventRegistrationController.isRegistrationOwner(#id)")
    public ResponseEntity<EventRegistrationResponse> getRegistrationById(@PathVariable Long id) {
        Optional<EventRegistration> registration = registrationRepository.findById(id);
        return registration.map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- UPDATE Registration Status ---
    // Only Organizers or Admins should be able to change status
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventRegistrationResponse> updateRegistrationStatus(@PathVariable Long id, @RequestParam RegistrationStatus newStatus) {
        EventRegistration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration with ID " + id + " not found."));

        registration.setStatus(newStatus);
        EventRegistration updatedRegistration = registrationRepository.save(registration);
        return ResponseEntity.ok(convertToDto(updatedRegistration));
    }

    // --- DELETE Registration ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN') or @eventRegistrationController.isRegistrationOwner(#id)")
    public ResponseEntity<HttpStatus> deleteRegistration(@PathVariable Long id) {
        if (registrationRepository.existsById(id)) {
            registrationRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}