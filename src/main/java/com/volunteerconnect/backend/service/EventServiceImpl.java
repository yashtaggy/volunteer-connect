package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventRequest;
import com.volunteerconnect.backend.dto.EventResponse;
import com.volunteerconnect.backend.dto.UserSummaryDto;
import com.volunteerconnect.backend.dto.organization.OrganizationSummaryDto;
import com.volunteerconnect.backend.exception.ResourceNotFoundException;
import com.volunteerconnect.backend.model.Event;
import com.volunteerconnect.backend.model.EventRegistration; // Import EventRegistration
import com.volunteerconnect.backend.model.RegistrationStatus; // Import RegistrationStatus
import com.volunteerconnect.backend.model.Role; // Import Role
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.EventRepository;
import com.volunteerconnect.backend.repository.EventRegistrationRepository; // Import EventRegistrationRepository
import com.volunteerconnect.backend.repository.organization.OrganizationRepository;
import com.volunteerconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final EventRegistrationRepository eventRegistrationRepository; // Inject EventRegistrationRepository

    @Autowired
    public EventServiceImpl(
            EventRepository eventRepository,
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            EventRegistrationRepository eventRegistrationRepository // Add to constructor
    ) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.eventRegistrationRepository = eventRegistrationRepository; // Initialize
    }

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest eventRequest, Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));

        if (organizer.getRole() != Role.ORGANIZER) {
            throw new IllegalArgumentException("Only users with role ORGANIZER can create events.");
        }

        // Ensure the eventRequest has an organizationId. This should come from the frontend
        // and ideally should be the organizer's organization.
        Long organizationId = eventRequest.getOrganizationId();
        if (organizationId == null) {
            throw new IllegalArgumentException("Organization ID must be provided for event creation.");
        }

        // Validate if the organizer belongs to the specified organization (security check)
        if (organizer.getOrganization() == null || !organizer.getOrganization().getId().equals(organizationId)) {
            throw new IllegalArgumentException("Organizer does not belong to the specified organization.");
        }

        Event event = convertToEntity(eventRequest, organizerId);
        event.setOrganizer(organizer); // Set the actual organizer object

        // Fetch the organization entity and set it to the event
        organizationRepository.findById(organizationId).ifPresent(event::setOrganization);
        if (event.getOrganization() == null) {
            throw new ResourceNotFoundException("Organization not found with id: " + organizationId);
        }

        Event savedEvent = eventRepository.save(event);
        return convertToDto(savedEvent);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return convertToDto(event);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest eventRequest, Long currentUserId) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // Authorization check: Only the organizer who created the event can update it
        if (!existingEvent.getOrganizer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You are not authorized to update this event.");
        }

        // Update fields
        existingEvent.setTitle(eventRequest.getTitle());
        existingEvent.setDescription(eventRequest.getDescription());
        existingEvent.setEventDate(eventRequest.getEventDate());
        existingEvent.setLocation(eventRequest.getLocation());
        existingEvent.setCapacity(eventRequest.getCapacity());
        existingEvent.setActive(eventRequest.isActive());
        existingEvent.setRequiredSkills(eventRequest.getRequiredSkills());
        existingEvent.setUpdatedDate(LocalDateTime.now());

        // Handle organization change if allowed (optional, complex, needs more checks)
        // For simplicity, we are not allowing organization change for an existing event for now.
        // If eventRequest.getOrganizationId() is different, you'd need to fetch and set it.

        Event updatedEvent = eventRepository.save(existingEvent);
        return convertToDto(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id, Long currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));

        // Authorization check: Only the organizer who created the event can delete it
        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You are not authorized to delete this event.");
        }

        eventRepository.delete(event);
    }


    // --- NEW: Implementation for event registration ---
    @Override
    @Transactional // Ensures the whole operation is atomic
    public EventResponse registerForEvent(Long eventId, Long volunteerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found with id: " + volunteerId));

        // Basic validation: Only VOLUNTEER role can register (or adjust as per your app's rules)
        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new IllegalArgumentException("Only users with role VOLUNTEER can register for events.");
        }

        // Check if event is active
        if (!event.isActive()) {
            throw new IllegalArgumentException("Cannot register for an inactive event.");
        }

        // Check if event is in the past
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot register for an event that has already occurred.");
        }

        // Check if already registered
        Optional<EventRegistration> existingRegistration = eventRegistrationRepository.findByEventAndVolunteer(event, volunteer);
        if (existingRegistration.isPresent()) {
            throw new IllegalArgumentException("Volunteer is already registered for this event.");
        }

        // Check capacity
        if (event.getRegisteredVolunteers().size() >= event.getCapacity()) {
            throw new IllegalArgumentException("Event is already at full capacity.");
        }

        // Create new registration
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .volunteer(volunteer)
                .status(RegistrationStatus.PENDING) // Default status, though @PrePersist handles this too
                .build();

        eventRegistrationRepository.save(registration);

        // Crucial: Update the collections on the event and volunteer side to reflect the new registration
        // (JPA will manage the actual database rows, but for the current transaction's context,
        // it's good to keep the entity relationships consistent).
        event.getRegistrations().add(registration);
        event.getRegisteredVolunteers().add(volunteer); // Assuming this list is for direct volunteers

        // If you were to fetch this event again after this transaction, the getRegisteredVolunteers()
        // would reflect the updated count due to the relationship mapping in Event.java.
        // We are returning the updated event DTO.
        return convertToDto(event);
    }
    // --- END NEW IMPLEMENTATION ---


    // Helper methods (already existing)
    @Override
    public Event convertToEntity(EventRequest eventRequest, Long organizerId) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found with id: " + organizerId));
        return Event.builder()
                .title(eventRequest.getTitle())
                .description(eventRequest.getDescription())
                .eventDate(eventRequest.getEventDate())
                .location(eventRequest.getLocation())
                .capacity(eventRequest.getCapacity())
                .requiredSkills(eventRequest.getRequiredSkills())
                .active(eventRequest.isActive())
                .organizer(organizer) // Set the organizer here
                .build();
    }

    @Override
    public EventResponse convertToDto(Event event) {
        UserSummaryDto organizerDto = null;
        if (event.getOrganizer() != null) {
            organizerDto = UserSummaryDto.builder()
                    .id(event.getOrganizer().getId())
                    .username(event.getOrganizer().getUsername())
                    .firstName(event.getOrganizer().getFirstName())
                    .lastName(event.getOrganizer().getLastName())
                    .email(event.getOrganizer().getEmail())
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

        // Collect registered volunteers from event.getRegisteredVolunteers()
        List<UserSummaryDto> registeredVolunteers = event.getRegisteredVolunteers().stream()
                .map(volunteer -> UserSummaryDto.builder()
                        .id(volunteer.getId())
                        .username(volunteer.getUsername())
                        .firstName(volunteer.getFirstName())
                        .lastName(volunteer.getLastName())
                        .email(volunteer.getEmail())
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
                .requiredSkills(event.getRequiredSkills())
                .organizer(organizerDto)
                .organization(organizationDto)
                .registeredVolunteers(registeredVolunteers) // Populate this from the event's collection
                .build();
    }
}