package com.volunteerconnect.backend.security;

import com.volunteerconnect.backend.model.User; // Import your User model
import com.volunteerconnect.backend.repository.UserRepository; // Import your UserRepository
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // Mark this class as a Spring Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Inject UserRepository into this service
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch the User entity from the database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Wrap the User entity in your CustomUserDetails object
        return new CustomUserDetails(user);
    }
}