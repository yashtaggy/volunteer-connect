package com.volunteerconnect.backend.security;

import com.volunteerconnect.backend.model.User; // Import User
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails; // Keep this import if you still use it elsewhere

import java.security.Key;
import java.time.Clock;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration; // in milliseconds

    private final Clock clock; // Inject Clock

    public JwtService(Clock clock) {
        this.clock = clock;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Change parameter type to User
    public String generateToken(User user) { // <--- CHANGE PARAMETER TO User
        return generateToken(new HashMap<>(), user);
    }

    // Change parameter type to User
    public String generateToken(Map<String, Object> extraClaims, User user) { // <--- CHANGE PARAMETER TO User
        Date now = Date.from(clock.instant());
        Date expirationDate = new Date(now.getTime() + jwtExpiration);

        // Add claims from User object
        extraClaims.put("userId", user.getId());
        extraClaims.put("role", user.getRole().name()); // Assuming role is an Enum in User
        extraClaims.put("email", user.getEmail());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());


        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername()) // Use username from User object
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(clock.instant()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}