package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventRegistrationResponse;
import com.volunteerconnect.backend.model.Event;
import com.volunteerconnect.backend.model.EventRegistration;
import com.volunteerconnect.backend.model.RegistrationStatus;
import com.volunteerconnect.backend.model.Role; // <--- ADD THIS IMPORT
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.EventRepository;
import com.volunteerconnect.backend.repository.EventRegistrationRepository;
import com.volunteerconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    public EventRegistrationServiceImpl(EventRepository eventRepository,
                                        UserRepository userRepository,
                                        EventRegistrationRepository eventRegistrationRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
    }

    private EventRegistrationResponse convertToDto(EventRegistration registration) {
        return EventRegistrationResponse.builder()
                .id(registration.getId())
                .eventId(registration.getEvent().getId())
                .eventTitle(registration.getEvent().getTitle())
                .volunteerId(registration.getVolunteer().getId())
                .volunteerUsername(registration.getVolunteer().getUsername())
                .registrationDate(registration.getRegistrationDate())
                .status(registration.getStatus())
                .build();
    }

    @Override
    public EventRegistrationResponse registerForEvent(Long eventId, Long volunteerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));

        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer with ID " + volunteerId + " not found."));

        // --- IMPORTANT ADDITION: Validate Volunteer Role ---
        if (volunteer.getRole() != Role.VOLUNTEER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users with VOLUNTEER role can register for events.");
        }
        // ----------------------------------------------------

        if (eventRegistrationRepository.findByEventAndVolunteer(event, volunteer).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Volunteer with ID " + volunteerId + " is already registered for event with ID " + eventId + ".");
        }

        long currentRegistrations = eventRegistrationRepository.findByEvent(event).stream()
                .filter(reg -> reg.getStatus() != RegistrationStatus.CANCELLED) // Only count active registrations
                .count();

        Integer capacity = event.getCapacity();
        if (capacity != null && currentRegistrations >= capacity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event capacity reached for event with ID " + eventId + ".");
        }

        EventRegistration newRegistration = EventRegistration.builder()
                .event(event)
                .volunteer(volunteer)
                .registrationDate(LocalDateTime.now()) // Set current time
                .status(RegistrationStatus.PENDING) // Set initial status
                .build();

        EventRegistration savedRegistration = eventRegistrationRepository.save(newRegistration);

        return convertToDto(savedRegistration);
    }

    @Override
    public void unregisterFromEvent(Long eventId, Long volunteerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));
        User volunteer = userRepository.findById(volunteerId) // Fetch volunteer to verify existence if needed
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer with ID " + volunteerId + " not found."));


        EventRegistration registration = eventRegistrationRepository.findByEventAndVolunteer(event, volunteer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found for volunteer " + volunteerId + " and event " + eventId + "."));

        // Ensure the current authenticated user is the one who made the registration
        if (!registration.getVolunteer().getId().equals(volunteerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to unregister from this event.");
        }

        // Add logic to prevent unregistering from already cancelled/rejected etc.
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration is already cancelled and cannot be unregistered again.");
        }
        // You might consider adding checks for past events etc.

        registration.setStatus(RegistrationStatus.CANCELLED);
        eventRegistrationRepository.save(registration);
    }

    @Override
    public List<EventRegistrationResponse> getRegistrationsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event with ID " + eventId + " not found."));

        List<EventRegistration> registrations = eventRegistrationRepository.findByEvent(event);
        return registrations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<EventRegistrationResponse> getRegistrationsByVolunteer(Long volunteerId) {
        User volunteer = userRepository.findById(volunteerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Volunteer with ID " + volunteerId + " not found."));

        List<EventRegistration> registrations = eventRegistrationRepository.findByVolunteer(volunteer);
        return registrations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRegistrationResponse getRegistrationById(Long registrationId) {
        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration with ID " + registrationId + " not found."));
        return convertToDto(registration);
    }
}