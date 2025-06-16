package com.volunteerconnect.backend.security;

import jakarta.servlet.FilterChain;        // Servlet API for filter chain
import jakarta.servlet.ServletException;     // Servlet API for exceptions
import jakarta.servlet.http.HttpServletRequest; // Servlet API for HTTP request
import jakarta.servlet.http.HttpServletResponse; // Servlet API for HTTP response

import org.springframework.lang.NonNull;      // For method parameter nullability annotation
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Spring Security auth token
import org.springframework.security.core.context.SecurityContextHolder; // Spring Security context holder
import org.springframework.security.core.userdetails.UserDetails; // Spring Security UserDetails
import org.springframework.security.core.userdetails.UserDetailsService; // Spring Security UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource; // For setting authentication details
import org.springframework.stereotype.Component; // Marks this as a Spring component
import org.springframework.web.filter.OncePerRequestFilter; // Ensures filter runs once per request

import java.io.IOException;

@Component // Makes this a Spring-managed component (bean)
public class JwtAuthenticationFilter extends OncePerRequestFilter { // Ensures filter is executed once per request

    private final JwtService jwtService;             // To handle JWT operations (validation, extraction)
    private final UserDetailsService userDetailsService; // To load user details from the database

    // Constructor for dependency injection
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,        // The incoming HTTP request
            @NonNull HttpServletResponse response,       // The HTTP response
            @NonNull FilterChain filterChain            // The chain of filters
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization"); // Get the Authorization header
        final String jwt;
        final String username;

        // 1. Check if Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // If not, just pass to the next filter
            return; // Exit the method
        }

        // 2. Extract the JWT token (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        // 3. Extract username from JWT
        username = jwtService.extractUsername(jwt);

        // 4. If username is found AND user is not already authenticated in the SecurityContext
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load UserDetails from our UserDetailsService (which uses UserRepository)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 5. Validate the token against the loaded UserDetails
            if (jwtService.validateToken(jwt, userDetails)) {
                // If token is valid, create an authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // Credentials are null for JWT authenticated users (already validated by token)
                        userDetails.getAuthorities() // Get user's roles/authorities
                );
                // Set additional details for the authentication (like IP address, session ID)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                // 6. Update the SecurityContextHolder with the authenticated user
                // This is crucial: it tells Spring Security that this user is authenticated for the current request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 7. Pass the request to the next filter in the chain
        filterChain.doFilter(request, response);
    }
}