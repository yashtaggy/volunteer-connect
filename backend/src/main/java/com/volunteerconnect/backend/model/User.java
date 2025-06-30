package com.volunteerconnect.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data; // Provides @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import com.volunteerconnect.backend.model.Role;


import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections; // For Collections.singletonList

@Data // This generates getters, setters for fields like username, password etc.
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users") // Good practice to use plural for table names
public class User implements UserDetails { // Ensure it implements UserDetails

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // IMPORTANT: This field must exist for getUsername()

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // Ensure you have a 'Role' enum defined

    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // --- UserDetails Interface Implementations ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Assign a role-based authority. Prefix with "ROLE_" is standard in Spring Security.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // Returns the username used to authenticate the user.
        return username;
    }

    @Override
    public String getPassword() {
        // Returns the password used to authenticate the user.
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // For simplicity, assume accounts do not expire
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // For simplicity, assume accounts are not locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // For simplicity, assume credentials do not expire
    }

    @Override
    public boolean isEnabled() {
        return true; // For simplicity, assume accounts are always enabled
    }

    // Lifecycle callbacks (optional, but good for auditing)
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