package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.UserResponseDto;
import com.volunteerconnect.backend.dto.UserUpdateDto;
import com.volunteerconnect.backend.model.User; // Still needed for getCurrentAuthenticatedUserId()
import com.volunteerconnect.backend.service.UserService; // IMPORTANT: This is the NEW UserService (interface)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // Inject the NEW UserService interface
    @Autowired
    private UserService userService; // Changed from UserRepository to UserService

    // Helper method to get the current authenticated user's ID
    // (Keep this in the controller as it's directly related to the security context of the request)
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

    // --- READ All Users ---
    // Only Admins can view all users
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        // DELEGATE TO SERVICE
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // --- READ Single User by ID ---
    // Admin can view any user, a user can view their own profile
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userController.isUserOwner(#id)")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        // DELEGATE TO SERVICE
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // --- UPDATE User ---
    // Admin can update any user, a user can update their own profile
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userController.isUserOwner(#id)")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userUpdateDto) {
        // DELEGATE TO SERVICE
        UserResponseDto updatedUser = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(updatedUser);
    }

    // --- DELETE User ---
    // Only Admins can delete users
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteUser(@PathVariable Long id) {
        // DELEGATE TO SERVICE
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Changed to NO_CONTENT as per typical DELETE success
    }
}