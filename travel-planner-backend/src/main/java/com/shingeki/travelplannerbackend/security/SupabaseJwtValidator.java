package com.shingeki.travelplannerbackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.PublicKey;
import java.util.Base64;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SupabaseJwtValidator {

    private final JwksPublicKeyProvider keyProvider;
    
    // 从环境变量获取 Supabase JWT 密钥，如果没有配置则为空字符串
    @Value("${supabase.jwt.secret:}")
    private String supabaseJwtSecret;

    public SupabaseJwtValidator(JwksPublicKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
    }

    public Claims validate(String token) {
        try {
            String cleanToken = token.replace("Bearer ", "").trim();

            // 解码头部以获取 kid 和 alg
            String[] chunks = cleanToken.split("\\.");
            if (chunks.length < 2) throw new JwtException("Invalid JWT format");

            // Base64Url 解码头部
            String headerJson = new String(Base64.getUrlDecoder().decode(chunks[0]));
            System.out.println("JWT Header: " + headerJson);

            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<java.util.Map<String, Object>> typeRef = new TypeReference<>() {
            };
            java.util.Map<String, Object> header = objectMapper.readValue(headerJson, typeRef);

            String kid = (String) header.get("kid");
            String alg = (String) header.get("alg");
            
            System.out.println("JWT kid: " + kid);
            System.out.println("JWT alg: " + alg);
            
            if (kid == null) throw new JwtException("Missing 'kid' in JWT header");
            if (alg == null) throw new JwtException("Missing 'alg' in JWT header");

            if ("HS256".equals(alg)) {
                // 使用 HMAC SHA256 验证（对称加密）
                return validateWithHmac(cleanToken);
            } else if ("RS256".equals(alg)) {
                // 使用 RSA 验证（非对称加密）
                return validateWithRsa(cleanToken, kid);
            } else {
                throw new JwtException("Unsupported algorithm: " + alg);
            }
        } catch (Exception e) {
            System.err.println("JWT validation error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("JWT validation failed: " + e.getMessage(), e);
        }
    }

    private Claims validateWithHmac(String token) {
        try {
            if (supabaseJwtSecret == null || supabaseJwtSecret.isEmpty()) {
                throw new RuntimeException("Supabase JWT secret is not configured. Please set the SUPABASE_JWT_SECRET environment variable.");
            }
            
            // 直接使用密钥字节而不是先Base64解码
            byte[] secretBytes = supabaseJwtSecret.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, "HmacSHA256");

            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(secretKeySpec)
                    .build()
                    .parseClaimsJws(token);

            return jws.getBody();
        } catch (Exception e) {
            System.err.println("HMAC JWT validation error: " + e.getMessage());
            throw new RuntimeException("HMAC JWT validation failed: " + e.getMessage(), e);
        }
    }

    private Claims validateWithRsa(String token, String kid) {
        try {
            PublicKey publicKey = keyProvider.getPublicKey(kid);
            
            if (publicKey == null) {
                throw new JwtException("Signing key cannot be null for kid: " + kid);
            }

            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);

            return jws.getBody();
        } catch (Exception e) {
            System.err.println("RSA JWT validation error: " + e.getMessage());
            throw new RuntimeException("RSA JWT validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * 验证 JWT 并返回用户 ID
     */
    public java.util.UUID validateTokenAndGetUserId(String authHeader) {
        Claims claims = validate(authHeader);
        String userIdStr = claims.getSubject(); // JWT 的 sub 字段包含用户 ID
        
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("User ID not found in JWT");
        }
        
        return java.util.UUID.fromString(userIdStr);
    }
}