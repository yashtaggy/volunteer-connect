package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.EventRequest;
import com.volunteerconnect.backend.dto.EventResponse;
import com.volunteerconnect.backend.dto.UserSummaryDto;
import com.volunteerconnect.backend.dto.organization.OrganizationSummaryDto;
import com.volunteerconnect.backend.model.Event;
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.model.organization.Organization;
import com.volunteerconnect.backend.repository.EventRepository;
import com.volunteerconnect.backend.repository.UserRepository;
import com.volunteerconnect.backend.repository.organization.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Keep this import
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    // Helper method to convert Event entity to EventResponse DTO
    private EventResponse convertToDto(Event event) {
        UserSummaryDto organizerDto = null;
        if (event.getOrganizer() != null) {
            organizerDto = UserSummaryDto.builder()
                    .id(event.getOrganizer().getId())
                    .username(event.getOrganizer().getUsername())
                    .email(event.getOrganizer().getEmail())
                    .firstName(event.getOrganizer().getFirstName())
                    .lastName(event.getOrganizer().getLastName())
                    .build();
        }

        OrganizationSummaryDto organizationDto = null;
        if (event.getOrganization() != null) {
            organizationDto = OrganizationSummaryDto.builder()
                    .id(event.getOrganization().getId())
                    .name(event.getOrganization().getName())
                    .contactEmail(event.getOrganization().getContactEmail())
                    .build();
        }

        List<UserSummaryDto> registeredVolunteers = event.getRegistrations().stream()
                .map(registration -> UserSummaryDto.builder()
                        .id(registration.getVolunteer().getId())
                        .username(registration.getVolunteer().getUsername())
                        .email(registration.getVolunteer().getEmail())
                        .firstName(registration.getVolunteer().getFirstName())
                        .lastName(registration.getVolunteer().getLastName())
                        .build())
                .collect(Collectors.toList());

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .location(event.getLocation())
                .capacity(event.getCapacity())
                .active(event.isActive())
                .organizer(organizerDto)
                .organization(organizationDto)
                .registeredVolunteers(registeredVolunteers)
                .build();
    }

    // --- CREATE Operation ---
    @PostMapping
    // NEW: Only Organizers or Admins can create events
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> createEvent(@RequestBody EventRequest eventRequest) {
        User organizer = userRepository.findById(eventRequest.getOrganizerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer with ID " + eventRequest.getOrganizerId() + " not found."));

        Organization organization = organizationRepository.findById(eventRequest.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with ID " + eventRequest.getOrganizationId() + " not found."));

        Event eventToSave = Event.builder()
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .eventDate(eventRequest.getEventDate())
                .location(eventRequest.getLocation())
                .capacity(eventRequest.getCapacity())
                .active(eventRequest.isActive())
                .organizer(organizer)
                .organization(organization)
                .build();

        Event savedEvent = eventRepository.save(eventToSave);
        return new ResponseEntity<>(convertToDto(savedEvent), HttpStatus.CREATED);
    }

    // --- READ Operations ---
    @GetMapping
    // Keep: Any authenticated user can view all events
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        List<EventResponse> eventResponses = events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventResponses);
    }

    @GetMapping("/{id}")
    // Keep: Any authenticated user can view an event by ID
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- UPDATE Operation ---
    @PutMapping("/{id}")
    // NEW: Only Organizers or Admins can update events
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id, @RequestBody EventRequest eventRequest) {
        Optional<Event> optionalEvent = eventRepository.findById(id);

        if (optionalEvent.isPresent()) {
            Event existingEvent = optionalEvent.get();

            User organizer = userRepository.findById(eventRequest.getOrganizerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer with ID " + eventRequest.getOrganizerId() + " not found."));

            Organization organization = organizationRepository.findById(eventRequest.getOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with ID " + eventRequest.getOrganizationId() + " not found."));

            existingEvent.setTitle(eventRequest.getTitle());
            existingEvent.setDescription(eventRequest.getDescription());
            existingEvent.setEventDate(eventRequest.getEventDate());
            existingEvent.setLocation(eventRequest.getLocation());
            existingEvent.setCapacity(eventRequest.getCapacity());
            existingEvent.setActive(eventRequest.isActive());
            existingEvent.setOrganizer(organizer);
            existingEvent.setOrganization(organization);

            Event updatedEvent = eventRepository.save(existingEvent);
            return ResponseEntity.ok(convertToDto(updatedEvent));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // --- DELETE Operation ---
    @DeleteMapping("/{id}")
    // NEW: Only Organizers or Admins can delete events
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<HttpStatus> deleteEvent(@PathVariable Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}