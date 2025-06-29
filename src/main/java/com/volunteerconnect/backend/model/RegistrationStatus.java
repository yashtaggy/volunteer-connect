package com.volunteerconnect.backend.model;

public enum RegistrationStatus {
    PENDING,     // Registration is awaiting confirmation (e.g., by organizer)
    CONFIRMED,   // Registration is confirmed
    CANCELLED,    // Registration has been cancelled by volunteer or organizer
    REJECTED
}