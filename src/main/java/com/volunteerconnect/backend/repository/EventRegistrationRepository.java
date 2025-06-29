package com.volunteerconnect.backend.repository;

import com.volunteerconnect.backend.model.Event;
import com.volunteerconnect.backend.model.EventRegistration;
import com.volunteerconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Custom method to find a registration by event and volunteer
    Optional<EventRegistration> findByEventAndVolunteer(Event event, User volunteer);

    // Optional: Methods to find all registrations for a specific event
    List<EventRegistration> findByEvent(Event event);

    // Optional: Methods to find all registrations by a specific volunteer
    List<EventRegistration> findByVolunteer(User volunteer);
}