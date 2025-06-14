package com.volunteerconnect.backend.repository;

import com.volunteerconnect.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Import this

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom query methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}