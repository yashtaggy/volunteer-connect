package com.volunteerconnect.backend.controller.organization;

import com.volunteerconnect.backend.dto.organization.OrganizationRequest;
import com.volunteerconnect.backend.dto.organization.OrganizationResponse;
import com.volunteerconnect.backend.model.organization.Organization;
import com.volunteerconnect.backend.repository.organization.OrganizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Make sure this is imported
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationRepository organizationRepository;

    // Helper method to convert Organization entity to OrganizationResponse DTO
    private OrganizationResponse convertToDto(Organization organization) {
        return OrganizationResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .address(organization.getAddress())
                .contactEmail(organization.getContactEmail())
                .phoneNumber(organization.getPhoneNumber())
                .websiteUrl(organization.getWebsiteUrl())
                .active(organization.isActive())
                .build();
    }

    // --- CREATE Operation ---
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')") // THIS IS THE CRITICAL ANNOTATION
    public ResponseEntity<OrganizationResponse> createOrganization(@RequestBody OrganizationRequest organizationRequest) {
        // You might want to add checks for duplicate organization names
        if (organizationRepository.findByName(organizationRequest.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization with this name already exists.");
        }

        Organization organizationToSave = Organization.builder()
                .name(organizationRequest.getName())
                .description(organizationRequest.getDescription())
                .address(organizationRequest.getAddress())
                .contactEmail(organizationRequest.getContactEmail())
                .phoneNumber(organizationRequest.getPhoneNumber())
                .websiteUrl(organizationRequest.getWebsiteUrl())
                .active(organizationRequest.isActive())
                .build();

        Organization savedOrganization = organizationRepository.save(organizationToSave);
        return new ResponseEntity<>(convertToDto(savedOrganization), HttpStatus.CREATED);
    }

    // --- READ Operations ---
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        List<Organization> organizations = organizationRepository.findAll();
        List<OrganizationResponse> organizationResponses = organizations.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(organizationResponses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable Long id) {
        Optional<Organization> organization = organizationRepository.findById(id);
        return organization.map(this::convertToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // --- UPDATE Operation ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<OrganizationResponse> updateOrganization(@PathVariable Long id, @RequestBody OrganizationRequest organizationRequest) {
        Optional<Organization> optionalOrganization = organizationRepository.findById(id);

        if (optionalOrganization.isPresent()) {
            Organization existingOrganization = optionalOrganization.get();

            existingOrganization.setName(organizationRequest.getName());
            existingOrganization.setDescription(organizationRequest.getDescription());
            existingOrganization.setAddress(organizationRequest.getAddress());
            existingOrganization.setContactEmail(organizationRequest.getContactEmail());
            existingOrganization.setPhoneNumber(organizationRequest.getPhoneNumber());
            existingOrganization.setWebsiteUrl(organizationRequest.getWebsiteUrl());
            existingOrganization.setActive(organizationRequest.isActive());

            Organization updatedOrganization = organizationRepository.save(existingOrganization);
            return ResponseEntity.ok(convertToDto(updatedOrganization));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // --- DELETE Operation ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteOrganization(@PathVariable Long id) {
        if (organizationRepository.existsById(id)) {
            organizationRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}