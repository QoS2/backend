package com.app.questofseoul.controller;

import com.app.questofseoul.dto.LoginRequest;
import com.app.questofseoul.dto.RegisterRequest;
import com.app.questofseoul.security.JwtTokenProvider;
import com.app.questofseoul.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "JWT 로그인(이메일/비밀번호), Google OAuth2 로그인, 회원가입, 현재 사용자 조회")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "JWT 로그인", description = "이메일과 비밀번호로 로그인하여 JWT 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        UUID userId = authService.login(request.getEmail(), request.getPassword());
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationSeconds();
        return ResponseEntity.ok(Map.of(
            "accessToken", accessToken,
            "expiresIn", expiresIn,
            "tokenType", "Bearer"
        ));
    }

    @Operation(summary = "회원가입", description = "이메일·비밀번호로 회원가입합니다. 가입 후 JWT 로그인으로 로그인할 수 있습니다.")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UUID userId = authService.register(
            request.getEmail(),
            request.getPassword(),
            request.getNickname()
        );
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationSeconds();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "accessToken", accessToken,
            "expiresIn", expiresIn,
            "tokenType", "Bearer"
        ));
    }

    @Operation(summary = "현재 사용자 조회", description = "JWT 또는 세션으로 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        UUID userId = authService.getCurrentUserId();
        return ResponseEntity.ok(Map.of("userId", userId.toString()));
    }

    @Operation(summary = "OAuth2 → JWT 토큰 발급", description = "Google OAuth2 로그인 후 세션이 있으면 JWT 액세스 토큰을 발급합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> issueToken() {
        UUID userId = authService.getCurrentUserId();
        String accessToken = jwtTokenProvider.generateAccessToken(userId);
        long expiresIn = jwtTokenProvider.getAccessTokenExpirationSeconds();
        return ResponseEntity.ok(Map.of(
            "accessToken", accessToken,
            "expiresIn", expiresIn,
            "tokenType", "Bearer"
        ));
    }
}
