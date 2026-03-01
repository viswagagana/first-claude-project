package com.payment.auth.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void createsAndParsesAccessToken() {
        JwtProperties props = new JwtProperties("test-secret-at-least-32-characters-long!!", 900_000L, 604800_000L, "test");
        JwtService jwt = new JwtService(props);
        String subject = UUID.randomUUID().toString();
        String token = jwt.createAccessToken(subject, "alice");
        assertThat(token).isNotBlank();
        JwtService.JwtClaims claims = jwt.parseAccessToken(token);
        assertThat(claims.userId().toString()).isEqualTo(subject);
        assertThat(claims.username()).isEqualTo("alice");
    }
}
