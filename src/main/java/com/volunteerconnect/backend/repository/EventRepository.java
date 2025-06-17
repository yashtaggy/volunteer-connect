package com.volunteerconnect.backend.repository;

import com.volunteerconnect.backend.model.Event; // Import your Event entity
import org.springframework.data.jpa.repository.JpaRepository; // Spring Data JPA's core repository interface
import org.springframework.stereotype.Repository; // Marks this as a Spring repository component

@Repository // Tells Spring this is a data repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // JpaRepository provides methods like save(), findById(), findAll(), delete() automatically.
    // You can add custom query methods here if needed, e.g.,
    // Optional<Event> findByTitle(String title);
    // List<Event> findByLocation(String location);
}