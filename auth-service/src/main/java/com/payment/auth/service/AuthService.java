package com.payment.auth.service;

import com.payment.auth.domain.AuditLog;
import com.payment.auth.domain.RefreshToken;
import com.payment.auth.domain.User;
import com.payment.auth.repository.AuditLogRepository;
import com.payment.auth.repository.RefreshTokenRepository;
import com.payment.auth.repository.UserRepository;
import com.payment.auth.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public LoginResult login(String username, String password, String ipAddress) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Account disabled");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            audit(user.getId(), "LOGIN_FAILED", "auth", "Invalid password", ipAddress);
            throw new IllegalArgumentException("Invalid credentials");
        }
        String accessToken = jwtService.createAccessToken(user.getId().toString(), user.getUsername());
        String refreshToken = jwtService.createRefreshToken(user.getId().toString());
        String tokenHash = hash(refreshToken);
        RefreshToken rt = RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(tokenHash)
            .expiresAt(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000L))
            .build();
        refreshTokenRepository.save(rt);
        audit(user.getId(), "LOGIN", "auth", null, ipAddress);
        return new LoginResult(accessToken, refreshToken, "Bearer", 900);
    }

    @Transactional
    public LoginResult refresh(String refreshTokenValue, String ipAddress) {
        String tokenHash = hash(refreshTokenValue);
        RefreshToken rt = refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token expired");
        }
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);
        User user = userRepository.findById(rt.getUserId()).orElseThrow();
        String accessToken = jwtService.createAccessToken(user.getId().toString(), user.getUsername());
        String newRefresh = jwtService.createRefreshToken(user.getId().toString());
        RefreshToken newRt = RefreshToken.builder()
            .userId(user.getId())
            .tokenHash(hash(newRefresh))
            .expiresAt(Instant.now().plusMillis(7 * 24 * 60 * 60 * 1000L))
            .build();
        refreshTokenRepository.save(newRt);
        audit(user.getId(), "REFRESH", "auth", null, ipAddress);
        return new LoginResult(accessToken, newRefresh, "Bearer", 900);
    }

    private static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void audit(UUID userId, String action, String resource, String details, String ipAddress) {
        try {
            auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .action(action)
                .resource(resource)
                .details(details)
                .ipAddress(ipAddress)
                .build());
        } catch (Exception ignored) {}
    }

    public record LoginResult(String accessToken, String refreshToken, String tokenType, long expiresInSeconds) {}
}
