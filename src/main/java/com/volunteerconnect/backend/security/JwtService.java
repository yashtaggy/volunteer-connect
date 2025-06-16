package com.volunteerconnect.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.time.Clock; // NEW: Import Clock

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION; // in milliseconds

    private final Clock clock; // NEW: Field for Clock

    // NEW CONSTRUCTOR: Inject Clock
    public JwtService(Clock clock) {
        this.clock = clock;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        // Use isTokenExpired method (which will now use the injected clock)
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        // Use clock.instant().toEpochMilli() for current time
        return extractExpiration(token).before(Date.from(clock.instant())); // IMPORTANT CHANGE HERE
    }

    // --- Methods to Generate a JWT ---

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String username) {
        // Use clock.instant().toEpochMilli() for issued at time
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(clock.instant())) // IMPORTANT CHANGE HERE
                .setExpiration(Date.from(clock.instant().plusMillis(JWT_EXPIRATION))) // IMPORTANT CHANGE HERE
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}