package com.volunteerconnect.backend.model.role;

public enum Role {
    VOLUNTEER,  // Default role for new user registrations
    ORGANIZER,  // Can create/manage events and organizations (maybe specific ones)
    ADMIN       // Full control over all entities and users
}