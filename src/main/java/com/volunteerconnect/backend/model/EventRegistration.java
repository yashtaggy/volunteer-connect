package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "event_registrations") // Table to store event sign-ups
public class EventRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many registrations can be for one event
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    // Many registrations can be by one user (volunteer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false) // Naming convention volunteer_id
    private User volunteer; // The User who is volunteering for this event

    @Column(nullable = false)
    private LocalDateTime registrationDate; // When the volunteer signed up

    @Enumerated(EnumType.STRING) // Store enum as String in DB
    @Column(nullable = false)
    private RegistrationStatus status; // E.g., PENDING, APPROVED, CANCELLED, COMPLETED

    // You could add other fields here like notes, hours contributed, etc.
}