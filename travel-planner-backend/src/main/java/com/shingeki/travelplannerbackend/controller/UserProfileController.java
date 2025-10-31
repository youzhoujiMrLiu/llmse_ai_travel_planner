package com.shingeki.travelplannerbackend.controller;

import com.shingeki.travelplannerbackend.security.JwksPublicKeyProvider;
import com.shingeki.travelplannerbackend.security.SupabaseJwtValidator;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class UserProfileController {

    private final SupabaseJwtValidator jwtValidator;
    private final JwksPublicKeyProvider keyProvider;

    public UserProfileController(SupabaseJwtValidator jwtValidator, JwksPublicKeyProvider keyProvider) {
        this.jwtValidator = jwtValidator;
        this.keyProvider = keyProvider;
    }

    @GetMapping("/api/user/profile")
    public ResponseEntity<Map<String, String>> getUserProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header"));
        }

        try {
            Claims claims = jwtValidator.validate(authHeader);
            return ResponseEntity.ok(Map.of("userId", claims.getSubject()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid token: " + e.getMessage()));
        }
    }
    
    @GetMapping("/api/public/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "OK", "message", "Service is running"));
    }
    
    @GetMapping("/api/public/jwks-status")
    public ResponseEntity<Map<String, Object>> jwksStatus() {
        return ResponseEntity.ok(Map.of(
            "jwksAvailable", keyProvider.isJwksAvailable(),
            "message", "JWKS status check completed"
        ));
    }
}