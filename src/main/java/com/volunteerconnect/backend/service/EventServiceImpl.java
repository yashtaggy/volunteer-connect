package com.volunteerconnect.backend.service;

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
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service // Marks this class as a Spring Service component
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository; // Now injecting OrganizationRepository

    @Autowired
    public EventServiceImpl(EventRepository eventRepository, UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    // --- Helper method to convert Event entity to EventResponse DTO ---
    // This method mirrors the logic you had in your controller's convertToDto
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

        // Handle registered volunteers - assuming event.getRegistrations() exists
        // and contains EventRegistration objects, each having a getVolunteer() method.
        List<UserSummaryDto> registeredVolunteers = event.getRegistrations() != null ?
                event.getRegistrations().stream()
                        .map(registration -> UserSummaryDto.builder()
                                .id(registration.getVolunteer().getId())
                                .username(registration.getVolunteer().getUsername())
                                .email(registration.getVolunteer().getEmail())
                                .firstName(registration.getVolunteer().getFirstName())
                                .lastName(registration.getVolunteer().getLastName())
                                .build())
                        .collect(Collectors.toList()) :
                List.of(); // Return empty list if no registrations

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .eventDate(event.getEventDate()) // Using eventDate as per your current controller
                .location(event.getLocation())
                .capacity(event.getCapacity())    // Using capacity as per your current controller
                .active(event.isActive())        // Using active as per your current controller
                .organizer(organizerDto)
                .organization(organizationDto)
                .registeredVolunteers(registeredVolunteers)
                .build();
    }

    @Override
    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        return events.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));
        return convertToDto(event);
    }

    @Override
    public EventResponse createEvent(EventRequest eventRequest, Long currentUserId) {
        // currentUserId is the authenticated user creating the event, who must be the organizer
        User organizer = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user (Organizer) with ID " + currentUserId + " not found."));

        // Fetch the organization based on the ID from the request DTO
        Organization organization = organizationRepository.findById(eventRequest.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with ID " + eventRequest.getOrganizationId() + " not found."));

        // Create a new Event entity from the DTO
        Event eventToSave = Event.builder()
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .eventDate(eventRequest.getEventDate())
                .location(eventRequest.getLocation())
                .capacity(eventRequest.getCapacity())
                .active(eventRequest.isActive())
                .organizer(organizer) // Set the actual organizer object
                .organization(organization) // Set the actual organization object
                .build();

        Event savedEvent = eventRepository.save(eventToSave);
        return convertToDto(savedEvent);
    }

    @Override
    public EventResponse updateEvent(Long eventId, EventRequest eventRequest, Long currentUserId) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));

        // Authorization check: User must be the organizer of the event OR an ADMIN
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        if (!existingEvent.getOrganizer().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this event.");
        }

        // Fetch updated organizer and organization from request DTO IDs
        // Note: Your current controller allows changing the organizer/organization during update.
        // If this is not desired, remove these lines and simply reuse existingEvent.getOrganizer()/getOrganization()
        User newOrganizer = userRepository.findById(eventRequest.getOrganizerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer with ID " + eventRequest.getOrganizerId() + " not found."));

        Organization newOrganization = organizationRepository.findById(eventRequest.getOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization with ID " + eventRequest.getOrganizationId() + " not found."));


        // Update fields from the DTO
        existingEvent.setTitle(eventRequest.getTitle());
        existingEvent.setDescription(eventRequest.getDescription());
        existingEvent.setEventDate(eventRequest.getEventDate());
        existingEvent.setLocation(eventRequest.getLocation());
        existingEvent.setCapacity(eventRequest.getCapacity());
        existingEvent.setActive(eventRequest.isActive());
        existingEvent.setOrganizer(newOrganizer); // Update organizer
        existingEvent.setOrganization(newOrganization); // Update organization

        Event updatedEvent = eventRepository.save(existingEvent);
        return convertToDto(updatedEvent);
    }

    @Override
    public void deleteEvent(Long eventId, Long currentUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));

        // Authorization check: User must be the organizer of the event OR an ADMIN
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found."));

        if (!event.getOrganizer().getId().equals(currentUserId) && !currentUser.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this event.");
        }

        eventRepository.delete(event);
    }
}