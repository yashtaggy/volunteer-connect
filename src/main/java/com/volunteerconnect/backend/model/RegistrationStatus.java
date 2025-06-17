package com.volunteerconnect.backend.model;

public enum RegistrationStatus {
    PENDING,       // Volunteer has signed up, awaiting approval (if applicable)
    APPROVED,      // Registration is confirmed
    CANCELLED,     // Registration was cancelled by volunteer or organizer
    COMPLETED      // Volunteer participated in the event (can be set after eventDate)
}