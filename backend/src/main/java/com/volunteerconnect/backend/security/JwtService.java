package com.volunteerconnect.backend.security;

import com.volunteerconnect.backend.model.User; // Import your User model
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.time.Clock;
import java.util.stream.Collectors;
import java.util.Collections; // Already there for Collections.singletonList, but good to ensure
import java.util.List; // <--- ADD THIS IMPORT


@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION; // in milliseconds

    private final Clock clock;

    public JwtService(Clock clock) {
        this.clock = clock;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // NEW: Extract roles from token claims
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        // Assuming roles are stored as a List<String> under the key "roles"
        // You might need to adjust "roles" if you use a different key
        if (claims.containsKey("roles")) {
            // JWT claims typically store lists as ArrayLists or similar,
            // so direct cast to List<String> should generally work if correctly populated.
            return (List<String>) claims.get("roles");
        }
        return Collections.emptyList();
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
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(clock.instant()));
    }

    // --- Methods to Generate a JWT ---

    // MODIFIED: Accepts UserDetails to get roles
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add roles to claims
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles); // <-- IMPORTANT: Add roles as a claim

        return createToken(claims, userDetails.getUsername());
    }

    // You can keep an overloaded generateToken if needed, but this one is preferred
    public String generateToken(User user) {
        // You can directly pass the User object if it implements UserDetails
        return generateToken((UserDetails) user);
    }


    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims) // Now these claims will contain the "roles"
                .setSubject(username)
                .setIssuedAt(Date.from(clock.instant()))
                .setExpiration(Date.from(clock.instant().plusMillis(JWT_EXPIRATION)))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}