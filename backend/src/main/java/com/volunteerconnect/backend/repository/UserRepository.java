package com.volunteerconnect.backend.repository; // Your existing package

import com.volunteerconnect.backend.model.User; // <--- ADD THIS LINE
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}