package com.volunteerconnect.backend.controller;

import com.volunteerconnect.backend.dto.UserProfileResponse;
import com.volunteerconnect.backend.dto.UserProfileUpdateRequest;
import com.volunteerconnect.backend.model.User; // Import your User model if you need to access its properties directly
import com.volunteerconnect.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // For @PreAuthorize
import org.springframework.security.core.Authentication; // To get the authenticated principal
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // Base path for user-related endpoints
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Fetches the profile of a specific user.
     * Accessible by ADMIN role or by the user themselves (if ID matches authenticated principal's ID).
     *
     * @param id The ID of the user to fetch.
     * @param authentication The Spring Security Authentication object.
     * @return UserProfileResponse containing user details.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Ensure this matches User.java's getId()
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long id, Authentication authentication) {
        // You can log the authenticated user's ID for debugging if needed:
        // User currentUser = (User) authentication.getPrincipal();
        // System.out.println("Authenticated user ID: " + currentUser.getId() + ", Requesting profile for ID: " + id);

        UserProfileResponse profile = userService.getUserProfileById(id);
        return ResponseEntity.ok(profile);
    }

    /**
     * Updates the profile of a specific user.
     * Accessible by ADMIN role or by the user themselves (if ID matches authenticated principal's ID).
     *
     * @param id The ID of the user to update.
     * @param request UserProfileUpdateRequest containing updated details.
     * @param authentication The Spring Security Authentication object.
     * @return UserProfileResponse of the updated user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id") // Ensure this matches User.java's getId()
    public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable Long id, @RequestBody UserProfileUpdateRequest request, Authentication authentication) {
        // You can add validation for the request body using @Valid here if needed
        // For example: public ResponseEntity<UserProfileResponse> updateUserProfile(@PathVariable Long id, @Valid @RequestBody UserProfileUpdateRequest request, ...

        UserProfileResponse updatedProfile = userService.updateUserProfile(id, request);
        return ResponseEntity.ok(updatedProfile);
    }
}