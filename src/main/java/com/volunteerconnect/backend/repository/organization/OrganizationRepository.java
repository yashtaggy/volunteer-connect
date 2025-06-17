package com.volunteerconnect.backend.repository.organization;

import com.volunteerconnect.backend.model.organization.Organization; // Make sure this is imported
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // NEW: Import Optional

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    // NEW: Find an organization by its name
    Optional<Organization> findByName(String name);

    // If you already have other methods, keep them
}