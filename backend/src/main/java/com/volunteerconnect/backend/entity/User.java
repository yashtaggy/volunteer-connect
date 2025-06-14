package com.volunteerconnect.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // NEW: Import SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List; // NEW: Import List

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // Renamed from 'user' to 'users' to avoid potential SQL keyword conflicts
public class User implements UserDetails { // Implement UserDetails

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

    // --- UserDetails Interface Implementations ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assign a default role "USER" to all users
        // You can implement more complex role management here later
        return List.of(new SimpleGrantedAuthority("ROLE_USER")); // IMPORTANT CHANGE: Return a default role
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // For simplicity, account never expires
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // For simplicity, account never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // For simplicity, credentials never expire
    }

    @Override
    public boolean isEnabled() {
        return true; // For simplicity, account is always enabled
    }
}