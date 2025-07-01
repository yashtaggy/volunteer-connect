//package com.volunteerconnect.backend.security;
//
//import com.volunteerconnect.backend.model.User; // Import your User model
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//
//public class CustomUserDetails implements UserDetails {
//
//    private final User user;
//    private Long id; // Crucial: Store the user's ID
//    private String username;
//    private String password; // Storing hashed password for UserDetails
//    private Collection<? extends GrantedAuthority> authorities;
//
//    public CustomUserDetails(User user) {
//        this.user = user;
//        this.id = user.getId();
//        this.username = user.getUsername();
//        this.password = user.getPassword();
//        // Assuming your User model has a getRole() method that returns a Role enum
//        // Convert your user's role to a Spring Security GrantedAuthority
//        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
//    }
//
//    // --- NEW: Getter for user ID ---
//    // This method is what EventController is trying to call (customUserDetails.getId())
//    public Long getId() {
//        return id;
//    }
//
//    public User getUser() {
//        return user;
//    }
//    // --- END NEW ---
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return username;
//    }
//
//    // All below methods typically return true for a basic active user
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}