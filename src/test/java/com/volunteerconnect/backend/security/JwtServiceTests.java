package com.volunteerconnect.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTests {

    private JwtService jwtService;

    private final String testSecret = "aVeryLongAndSecureSecretKeyForTestingPurposes1234567890abcdef";
    private final long jwtExpirationMs = 60 * 60 * 1000; // 1 hour

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        // Dynamically set fixedClock to current time
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
        String token = jwtService.generateToken(username);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(username, jwtService.extractUsername(token));
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        UserDetails userDetails = new User("testUser", "password", new ArrayList<>());

        String token = createTestToken(
                new HashMap<>(),
                userDetails.getUsername(),
                fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli()
        );

        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        String username = "testUser";
        long expiredTime = fixedClock.instant().minusSeconds(60).toEpochMilli(); // already expired
        String token = createTestToken(new HashMap<>(), username, expiredTime);
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
            jwtService.validateToken(token, userDetails);
        });
    }

    @Test
    void extractExpiration_shouldReturnCorrectDate() {
        long expirationMillis = fixedClock.instant().plusMillis(jwtExpirationMs).toEpochMilli();
        String token = createTestToken(new HashMap<>(), "testuser", expirationMillis);
        Date extractedExpiration = jwtService.extractExpiration(token);
        assertEquals(expirationMillis, extractedExpiration.getTime(), 500); // 0.5 sec tolerance
    }
}
