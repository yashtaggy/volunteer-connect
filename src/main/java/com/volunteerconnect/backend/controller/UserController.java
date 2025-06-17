package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.UserResponseDto; // This DTO is needed
import com.volunteerconnect.backend.dto.UserUpdateDto; // This DTO is needed
import com.volunteerconnect.backend.model.User;
import com.volunteerconnect.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Helper method to get the current authenticated user's ID
    // This assumes your Authentication.getPrincipal() returns your User entity directly
    // or has a method to get the ID. Adjust if your UserDetails implementation is different.
    private Long getCurrentAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        // This case should ideally not be reached if @PreAuthorize("isAuthenticated()") or similar is used
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated or principal not a User instance.");
    }

    // Custom security method for @PreAuthorize to check if the current user owns the profile
    // Make sure this controller is a Spring Bean by having @RestController or @Component
    public boolean isUserOwner(Long userId) {
        return getCurrentAuthenticatedUserId().equals(userId);
    }

    // Helper method to convert User entity to UserResponseDto
    private UserResponseDto convertToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole()) // Include role in response
                .build();
    }

    // --- READ All Users ---
    // Only Admins can view all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponseDto> userResponses = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // --- READ Single User by ID ---
    // Admin can view any user, a user can view their own profile
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userController.isUserOwner(#id)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- UPDATE User ---
    // Admin can update any user, a user can update their own profile
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userController.isUserOwner(#id)")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + id + " not found."));

        // Update fields that are allowed to be changed
        user.setFirstName(userUpdateDto.getFirstName());
        user.setLastName(userUpdateDto.getLastName());
        user.setEmail(userUpdateDto.getEmail());
        // IMPORTANT: Do NOT allow direct role update via this endpoint for non-admins for security.
        // The UserUpdateDto should generally not contain 'role' for this reason.
        // If it does, you'd need additional logic here:
        // if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
        //    user.setRole(userUpdateDto.getRole()); // Only allow admin to change role
        // }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(convertToDto(updatedUser));
    }

    // --- DELETE User ---
    // Only Admins can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}