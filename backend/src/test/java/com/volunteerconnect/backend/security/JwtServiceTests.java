package com.volunteerconnect.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit; // NEW: Import for TimeUnit

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTests {

    @InjectMocks
    private JwtService jwtService;

    private final String TEST_SECRET_KEY = "c2VjcmV0S2V5Rm9yVGVzdGluZ1B1cnBvc2VzMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=";
    private final long TEST_JWT_EXPIRATION = 1000 * 60 * 60; // 1 hour in milliseconds

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", TEST_JWT_EXPIRATION);
    }

    // Helper method to get the signing key (copied from JwtService)
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(TEST_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);

        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(username, "encodedpassword", new HashMap().values());

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidUsername() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User("wronguser", "encodedpassword", new HashMap().values());

        assertFalse(jwtService.validateToken(token, userDetails));
    }

    // MODIFIED TEST FOR EXPIRED TOKEN
    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        String username = "testuser";

        // Manually create a token that expired 1 hour ago
        Date now = new Date(System.currentTimeMillis());
        Date issuedAt = new Date(now.getTime() - TimeUnit.HOURS.toMillis(2)); // Issued 2 hours ago
        Date expiration = new Date(now.getTime() - TimeUnit.HOURS.toMillis(1)); // Expired 1 hour ago

        Map<String, Object> claims = new HashMap<>();
        String expiredToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(issuedAt) // Set issued at in the past
                .setExpiration(expiration) // Set expiration in the past
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(username, "encodedpassword", new HashMap().values());

        assertFalse(jwtService.validateToken(expiredToken, userDetails));
    }

    @Test
    void extractExpiration_shouldReturnCorrectDate() {
        String username = "testuser";
        String token = jwtService.generateToken(username);

        Date expiration = jwtService.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.getTime() > System.currentTimeMillis());
        assertTrue(expiration.getTime() < (System.currentTimeMillis() + TEST_JWT_EXPIRATION + 1000));
    }
}