package com.payment.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
    String secret,
    long accessTokenValidityMs,
    long refreshTokenValidityMs,
    String issuer
) {
    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("app.jwt.secret must be set");
        }
        if (accessTokenValidityMs <= 0) accessTokenValidityMs = 900_000; // 15 min
        if (refreshTokenValidityMs <= 0) refreshTokenValidityMs = 7 * 24 * 60 * 60 * 1000L; // 7 days
        if (issuer == null) issuer = "payment-auth";
    }
}
