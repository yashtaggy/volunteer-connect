// C:/Users/Yash/volunteer-connect/backend/src/main/java/com/volunteerconnect/backend/model/role/Role.java
// Make sure this file is moved to C:/Users/Yash/volunteer-connect/backend/src/main/java/com/volunteerconnect/backend/model/
package com.volunteerconnect.backend.model; // <--- This line is changed

public enum Role {
    VOLUNTEER,  // Default role for new user registrations
    ORGANIZER,  // Can create/manage events and organizations (maybe specific ones)
    ADMIN       // Full control over all entities and users
}