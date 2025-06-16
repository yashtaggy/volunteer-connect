package com.volunteerconnect.backend.service;

import com.volunteerconnect.backend.model.User; // Import the User entity from its model package
import com.volunteerconnect.backend.repository.UserRepository; // Import the UserRepository from its repository package

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection for UserRepository and PasswordEncoder
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user by encoding their password and saving them to the database.
     * @param user The User object containing registration details.
     * @return The saved User object.
     */
    public User registerNewUser(User user) {
        // Encode password before saving to the database for security
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    /**
     * Finds a user by their username.
     * @param username The username to search for.
     * @return An Optional containing the User if found, or an empty Optional otherwise.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}