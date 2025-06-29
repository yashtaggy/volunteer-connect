package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.dto.EventRegistrationResponse;
import java.util.List;

public interface EventRegistrationService {

    /**
     * Registers a volunteer for an event.
     * @param eventId The ID of the event to register for.
     * @param volunteerId The ID of the volunteer (current authenticated user).
     * @return The details of the newly created registration.
     */
    EventRegistrationResponse registerForEvent(Long eventId, Long volunteerId);

    /**
     * Unregisters a volunteer from an event.
     * @param eventId The ID of the event to unregister from.
     * @param volunteerId The ID of the volunteer (current authenticated user).
     */
    void unregisterFromEvent(Long eventId, Long volunteerId);

    /**
     * Retrieves all registrations for a specific event.
     * @param eventId The ID of the event.
     * @return A list of registration responses for the event.
     */
    List<EventRegistrationResponse> getRegistrationsForEvent(Long eventId);

    /**
     * Retrieves all registrations made by a specific volunteer.
     * @param volunteerId The ID of the volunteer.
     * @return A list of registration responses made by the volunteer.
     */
    List<EventRegistrationResponse> getRegistrationsByVolunteer(Long volunteerId);

    /**
     * Finds a specific registration by its ID.
     * @param registrationId The ID of the registration.
     * @return The registration response.
     */
    EventRegistrationResponse getRegistrationById(Long registrationId);
}