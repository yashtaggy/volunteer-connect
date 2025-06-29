package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "volunteer_id"}) // Ensures a volunteer can only register once per event
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer; // This is the user who registers as a volunteer

    private LocalDateTime registrationDate; // When the registration occurred

    // --- ADD THIS FIELD ---
    @Enumerated(EnumType.STRING) // Store enum as String in DB
    private RegistrationStatus status;
    // ----------------------

    @PrePersist // Set registrationDate automatically before persisting
    protected void onCreate() {
        if (registrationDate == null) {
            registrationDate = LocalDateTime.now();
        }
        // Set default status if not already set during build
        if (status == null) {
            status = RegistrationStatus.PENDING; // Default to PENDING for new registrations
        }
    }
}