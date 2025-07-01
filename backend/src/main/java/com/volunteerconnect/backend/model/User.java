package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority; // Import
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Import
import org.springframework.security.core.userdetails.UserDetails; // Import

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List; // For List.of()

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
// Make User implement UserDetails
public class User implements UserDetails { // <--- ADD "implements UserDetails"

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedDate;

    // --- UserDetails interface methods ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return a list of authorities based on the user's role
        // Example: List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password; // Your existing password field
    }

    @Override
    public String getUsername() {
        return username; // Your existing username field
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // For simplicity, always true
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // For simplicity, always true
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // For simplicity, always true
    }

    @Override
    public boolean isEnabled() {
        return true; // For simplicity, always true
    }
}