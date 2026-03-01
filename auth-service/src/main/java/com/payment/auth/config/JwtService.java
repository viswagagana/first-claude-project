package com.payment.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String subject, String username) {
        return Jwts.builder()
            .subject(subject)
            .claim("username", username)
            .issuer(properties.issuer())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.accessTokenValidityMs()))
            .signWith(key)
            .compact();
    }

    public String createRefreshToken(String subject) {
        return Jwts.builder()
            .subject(subject)
            .claim("type", "refresh")
            .issuer(properties.issuer())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + properties.refreshTokenValidityMs()))
            .signWith(key)
            .compact();
    }

    public JwtClaims parseAccessToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token)
            .getPayload();
        return new JwtClaims(
            UUID.fromString(claims.getSubject()),
            claims.get("username", String.class)
        );
    }

    public String getSubject(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public record JwtClaims(UUID userId, String username) {}
}
