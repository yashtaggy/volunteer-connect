package com.volunteerconnect.backend.model.organization; // Or com.volunteerconnect.backend.model.organization if you made a subpackage

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "organizations") // This will be the table name in your database
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Name of the organization

    @Column(columnDefinition = "TEXT")
    private String description; // About the organization

    private String contactEmail; // Main contact email for the organization

    private String phoneNumber; // Optional phone number

    private String websiteUrl; // Optional website URL

    private String address; // Physical address or main location

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true; // Is the organization currently active?

    // You might consider adding relationships here later, e.g.,
    // @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Event> hostedEvents = new ArrayList<>();
    // (We'll add the event side of the relationship in the Event entity next)
}