package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.volunteerconnect.backend.model.Role;
import com.volunteerconnect.backend.model.organization.Organization; // <-- NEW IMPORT

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // --- ADD THIS NEW RELATIONSHIP ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id") // This column will be created in the users table
    private Organization organization; // The organization the user belongs to (e.g., if they are an ORGANIZER)
    // IMPORTANT: Consider if this should be nullable=false.
    // If every user *must* belong to an org, make it nullable=false.
    // For flexibility during initial setup, I'll leave it nullable (default).
    // If Event.organization is nullable=false, and User.organization is nullable,
    // we'll need to handle the case where an organizer might not have an assigned org.
    // For now, let's proceed with it being nullable in User for initial setup.
    // --- END NEW RELATIONSHIP ---


    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}