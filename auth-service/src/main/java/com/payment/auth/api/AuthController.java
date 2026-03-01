package com.payment.auth.api;

import com.payment.auth.domain.User;
import com.payment.auth.repository.UserRepository;
import com.payment.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and token management")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Returns JWT access and refresh tokens")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid credentials")
    public ResponseEntity<AuthService.LoginResult> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        if (httpRequest.getHeader("X-Forwarded-For") != null) {
            ip = httpRequest.getHeader("X-Forwarded-For").split(",")[0].trim();
        }
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword(), ip));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Issue new access and refresh tokens")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    public ResponseEntity<AuthService.LoginResult> refresh(
        @Valid @RequestBody RefreshRequest request,
        HttpServletRequest httpRequest
    ) {
        String ip = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken(), ip));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Create a new user account")
    @ApiResponse(responseCode = "201", description = "Created")
    @ApiResponse(responseCode = "400", description = "Username or email already exists")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .enabled(true)
            .build();
        userRepository.save(user);
        return ResponseEntity.created(URI.create("/api/v1/users/" + user.getId())).build();
    }
}
