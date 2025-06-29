package com.volunteerconnect.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply CORS to all paths under /api/
                .allowedOrigins("http://localhost:5173") // Allow your React frontend
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers (or specify "Authorization", "Content-Type")
                .allowCredentials(true) // Allow credentials (cookies, auth headers)
                .maxAge(3600); // Cache preflight response for 1 hour
    }
}