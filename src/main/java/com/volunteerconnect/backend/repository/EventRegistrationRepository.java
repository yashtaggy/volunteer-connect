package com.volunteerconnect.backend.repository;

import com.volunteerconnect.backend.model.Event; // NEW: Import Event entity
import com.volunteerconnect.backend.model.EventRegistration; // Import your EventRegistration entity
import com.volunteerconnect.backend.model.User; // NEW: Import User entity
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA's core repository interface
import org.springframework.stereotype.Repository; // Marks this as a Spring repository component

import java.util.List; // NEW: Import List for return types

@Repository // Tells Spring this is a data repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    // JpaRepository provides methods like save(), findById(), findAll(), delete() automatically.

    // Custom query methods for fetching registrations by Event or Volunteer
    List<EventRegistration> findByEvent(Event event);

    List<EventRegistration> findByVolunteer(User volunteer);

    // You can add other custom query methods here if needed, for example:
    // Optional<EventRegistration> findByEventAndVolunteer(Event event, User volunteer);
}