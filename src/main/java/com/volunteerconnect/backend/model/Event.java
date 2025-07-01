package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set; // Import Set if you prefer for ManyToMany, but List is also fine
import java.util.HashSet; // Import HashSet if using Set

import com.volunteerconnect.backend.model.organization.Organization;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String location;

    private int capacity;

    private String requiredSkills; // To store comma-separated skills from frontend

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    // --- NEW: For automatic creation and update timestamps ---
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now(); // Initialize updatedDate on creation too
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    // --- END NEW TIMESTAMPS ---

    // --- NEW: Link to EventRegistration for all registrations ---
    // This maintains the link to the EventRegistration join table entries
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EventRegistration> registrations = new ArrayList<>();
    // --- END NEW REGISTRATIONS LINK ---

    // --- NEW: Mapping for registered volunteers (Users) through the EventRegistration join table ---
    // This allows you to directly get the list of Users (volunteers) associated with this event
    @ManyToMany
    @JoinTable(
            name = "event_registrations", // The existing join table
            joinColumns = @JoinColumn(name = "event_id"), // Column in event_registrations that links to 'event'
            inverseJoinColumns = @JoinColumn(name = "volunteer_id") // Column in event_registrations that links to 'user' (volunteer)
    )
    @Builder.Default
    private List<User> registeredVolunteers = new ArrayList<>(); // Use List if order matters, otherwise Set
    // --- END NEW REGISTERED VOLUNTEERS ---
}