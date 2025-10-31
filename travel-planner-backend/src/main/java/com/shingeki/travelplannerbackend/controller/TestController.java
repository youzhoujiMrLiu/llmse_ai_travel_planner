package com.shingeki.travelplannerbackend.controller;

import com.shingeki.travelplannerbackend.security.JwksPublicKeyProvider;
import com.shingeki.travelplannerbackend.security.SupabaseJwtValidator;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final SupabaseJwtValidator jwtValidator;
    private final JwksPublicKeyProvider keyProvider;

    public TestController(SupabaseJwtValidator jwtValidator, JwksPublicKeyProvider keyProvider) {
        this.jwtValidator = jwtValidator;
        this.keyProvider = keyProvider;
    }

    @GetMapping("/me")
    public String me(@RequestHeader("Authorization") String authHeader) {
        Claims claims = jwtValidator.validate(authHeader);
        String userId = claims.getSubject(); // Supabase user ID (UUID)
        return "Authenticated user: " + userId;
    }
    
    @GetMapping("/jwks-status")
    public String jwksStatus() {
        boolean available = keyProvider.isJwksAvailable();
        return "JWKS availability: " + available;
    }
}