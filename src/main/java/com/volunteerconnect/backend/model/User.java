package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.volunteerconnect.backend.model.role.Role; // UPDATED: Import Role enum from its specific package

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // --- User Role ---
    @Enumerated(EnumType.STRING) // Stores the enum name (e.g., "VOLUNTEER") as a string in the database
    @Column(nullable = false)
    @Builder.Default // Lombok annotation to use the default initialization below
    private Role role = Role.VOLUNTEER; // Default role for new users is VOLUNTEER
    // -------------------

    @Column(nullable = false, unique = true)
    private String email;

    private String firstName;
    private String lastName;

    // UserDetails methods implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name())); // IMPORTANT: Prefix with "ROLE_" for Spring Security
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
}