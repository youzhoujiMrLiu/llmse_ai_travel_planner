package com.shingeki.travelplannerbackend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwksPublicKeyProvider {

    @Value("${supabase.project-ref}") // 例如: abc123.supabase.co
    private String projectRef;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private volatile Instant lastFetched = Instant.MIN;
    private volatile boolean fetchAttempted = false; // 标记是否尝试过获取 JWKS

    private static final Duration CACHE_TTL = Duration.ofHours(1); // 缓存1小时

    public PublicKey getPublicKey(String kid) {
        // 缓存未过期且存在
        if (Instant.now().minus(CACHE_TTL).isBefore(lastFetched) && keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }

        synchronized (this) {
            // 双重检查
            if (Instant.now().minus(CACHE_TTL).isBefore(lastFetched) && keyCache.containsKey(kid)) {
                return keyCache.get(kid);
            }

            // 如果之前尝试过获取但失败了，避免重复尝试导致的延迟
            if (fetchAttempted && keyCache.isEmpty()) {
                throw new RuntimeException("Previous attempt to fetch JWKS failed. Skipping fetch to avoid repeated failures.");
            }

            fetchAndCacheKeys();
            
            // 检查是否成功获取到指定kid的公钥
            if (!keyCache.containsKey(kid)) {
                throw new RuntimeException("Public key not found for kid: " + kid + 
                    ". Available keys: " + keyCache.keySet());
            }
            
            return keyCache.get(kid);
        }
    }

    private void fetchAndCacheKeys() {
        fetchAttempted = true; // 标记已尝试获取
        
        try {
            String jwksUrl = "https://" + projectRef + "/auth/v1/.well-known/jwks.json";
            System.out.println("Fetching JWKS from: " + jwksUrl);
            
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                                .uri(java.net.URI.create(jwksUrl))
                                .build();
                                
            java.net.http.HttpResponse<String> response = client.send(request, 
                    java.net.http.HttpResponse.BodyHandlers.ofString());
            
            System.out.println("JWKS Response Status: " + response.statusCode());
            System.out.println("JWKS Response Body: " + response.body());
            
            if (response.statusCode() != 200) {
                System.err.println("Failed to fetch JWKS from Supabase. Status code: " + response.statusCode());
                return; // 不抛出异常，让应用可以继续运行
            }
            
            String json = response.body();

            JwksResponse responseObj = objectMapper.readValue(json, JwksResponse.class);
            System.out.println("Parsed JWKS response with " + (responseObj.keys != null ? responseObj.keys.size() : 0) + " keys");

            keyCache.clear();
            if (responseObj.keys != null) {
                for (JwksKey key : responseObj.keys) {
                    if (key != null && "RSA".equals(key.kty) && "sig".equals(key.use) && "RS256".equals(key.alg)) {
                        try {
                            PublicKey pubKey = buildRsaPublicKey(key.modulus, key.exponent);
                            keyCache.put(key.kid, pubKey);
                            System.out.println("Added public key for kid: " + key.kid);
                        } catch (Exception e) {
                            System.err.println("Failed to build public key for kid: " + key.kid + ", error: " + e.getMessage());
                        }
                    }
                }
            }
            lastFetched = Instant.now();
            System.out.println("Total keys cached: " + keyCache.size());
        } catch (Exception e) {
            System.err.println("Error fetching or parsing JWKS: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，让应用可以继续运行
        }
    }

    private PublicKey buildRsaPublicKey(String modulus, String exponent) {
        try {
            if (modulus == null || exponent == null) {
                throw new IllegalArgumentException("Modulus or exponent is null");
            }
            
            byte[] nBytes = Base64.getUrlDecoder().decode(modulus);
            byte[] eBytes = Base64.getUrlDecoder().decode(exponent);

            BigInteger n = new BigInteger(1, nBytes);
            BigInteger e = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new RuntimeException("Failed to build RSA public key", ex);
        }
    }
    
    // 用于测试 JWKS 是否可访问的方法
    public boolean isJwksAvailable() {
        return !keyCache.isEmpty();
    }
}