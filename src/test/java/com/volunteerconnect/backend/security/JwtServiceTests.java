package com.volunteerconnect.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any; // <--- NEW: Import any() for mocking generic types

class JwtServiceTests {

    private JwtService jwtService;

    private final String testSecret = "aVeryLongAndSecureSecretKeyForTestingPurposes1234567890abcdef";
    private final long jwtExpirationMs = 60 * 60 * 1000; // 1 hour

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.now(), ZoneOffset.UTC);
        jwtService = new JwtService(fixedClock);
        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", testSecret);
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", jwtExpirationMs);
    }

    private String createTestToken(Map<String, Object> claims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(fixedClock.instant()))
                .setExpiration(new Date(expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(testSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String username = "testUser";
        String token = createTestToken(
                new HashMap<>(),
                username,
                fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli()
        );
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void extractClaim_shouldReturnCorrectClaim() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");

        String token = createTestToken(
                claims,
                "testUser",
                fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli()
        );
        assertEquals("ADMIN", jwtService.extractClaim(token, c -> c.get("role", String.class)));
    }

    @Test
    void generateToken_shouldGenerateValidToken() {
        String username = "testUser";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        List<GrantedAuthority> authoritiesList = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        // CORRECTED LINE: Using any() matcher for the collection return type
        when(userDetails.getAuthorities()).thenAnswer(invocation -> {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        });

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtService.extractUsername(token));

        List<String> extractedRoles = jwtService.extractRoles(token);
        assertFalse(extractedRoles.isEmpty());
        assertTrue(extractedRoles.contains("ROLE_USER"));
    }


    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        UserDetails userDetails = new User("testUser", "password", Collections.emptyList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Collections.singletonList("ROLE_USER"));

        String token = createTestToken(
                claims,
                userDetails.getUsername(),
                fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli()
        );

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        String username = "testUser";
        long expiredTime = fixedClock.instant().minusSeconds(60).toEpochMilli();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Collections.singletonList("ROLE_USER"));

        String token = createTestToken(claims, username, expiredTime);
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.validateToken(token, userDetails);
        });
    }

    @Test
    void extractExpiration_shouldReturnCorrectDate() {
        long expirationMillis = fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli();
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", Collections.singletonList("ROLE_USER"));

        String token = createTestToken(claims, "testuser", expirationMillis);
        Date extractedExpiration = jwtService.extractExpiration(token);
        assertEquals(expirationMillis, extractedExpiration.getTime(), 500);
    }

    @Test
    void extractRoles_shouldReturnCorrectRoles() {
        Map<String, Object> claims = new HashMap<>();
        List<String> expectedRoles = Arrays.asList("ROLE_ORGANIZER", "ROLE_USER");
        claims.put("roles", expectedRoles);

        String token = createTestToken(claims, "testUserWithRoles", fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli());

        List<String> actualRoles = jwtService.extractRoles(token);
        assertNotNull(actualRoles);
        assertFalse(actualRoles.isEmpty());
        assertEquals(2, actualRoles.size());
        assertTrue(actualRoles.containsAll(expectedRoles));
    }

    @Test
    void extractRoles_shouldReturnEmptyListIfNoRolesClaim() {
        String token = createTestToken(new HashMap<>(), "testUserNoRoles", fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli());
        List<String> actualRoles = jwtService.extractRoles(token);
        assertNotNull(actualRoles);
        assertTrue(actualRoles.isEmpty());
    }
}