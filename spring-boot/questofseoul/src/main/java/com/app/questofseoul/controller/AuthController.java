package com.app.questofseoul.controller;

import com.app.questofseoul.config.JwtProperties;
import com.app.questofseoul.domain.enums.UserRole;
import com.app.questofseoul.dto.LoginRequest;
import com.app.questofseoul.dto.RegisterRequest;
import com.app.questofseoul.exception.AuthenticationException;
import com.app.questofseoul.security.JwtTokenProvider;
import com.app.questofseoul.service.AuthService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "인증", description = "로그인, 회원가입, 현재 사용자 조회")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Operation(summary = "JWT 로그인", description = "로그인하여 JWT 액세스 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response
    ) {
        UUID userId = authService.login(request.getEmail(), request.getPassword());
        UserRole role = authService.resolveRole(userId);
        String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        setRefreshTokenCookie(response, refreshToken);
        return ResponseEntity.ok(buildTokenResponse(accessToken));
    }

    @Operation(summary = "회원가입", description = "가입 후 JWT 로그인으로 로그인할 수 있습니다.")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletResponse response
    ) {
        UUID userId = authService.register(
            request.getEmail(),
            request.getPassword(),
            request.getNickname()
        );
        UserRole role = authService.resolveRole(userId);
        String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        setRefreshTokenCookie(response, refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(buildTokenResponse(accessToken));
    }

    @Operation(summary = "현재 사용자 조회", description = "JWT 또는 세션으로 인증된 사용자 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser() {
        UUID userId = authService.getCurrentUserId();
        UserRole role = authService.resolveRole(userId);
        return ResponseEntity.ok(Map.of(
                "userId", userId.toString(),
                "role", role.name()
        ));
    }

    @Operation(summary = "OAuth2 -> JWT 토큰 발급", description = "로그인 후 세션이 있으면 JWT 액세스 토큰을 발급합니다.")
    @SecurityRequirement(name = "sessionAuth")
    @PostMapping("/token")
    public ResponseEntity<Map<String, Object>> issueToken(HttpServletResponse response) {
        UUID userId = authService.getCurrentUserId();
        UserRole role = authService.resolveRole(userId);
        String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
        setRefreshTokenCookie(response, refreshToken);
        return ResponseEntity.ok(buildTokenResponse(accessToken));
    }

    @Operation(summary = "토큰 갱신", description = "HttpOnly 리프레시 토큰 쿠키로 액세스 토큰을 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthenticationException("리프레시 토큰이 없습니다.");
        }

        try {
            UUID userId = jwtTokenProvider.getUserIdFromRefreshToken(refreshToken);
            UserRole role = authService.resolveRole(userId);
            String newAccessToken = jwtTokenProvider.generateAccessToken(userId, role);
            String rotatedRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
            setRefreshTokenCookie(response, rotatedRefreshToken);
            return ResponseEntity.ok(buildTokenResponse(newAccessToken));
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            clearRefreshTokenCookie(response);
            throw new AuthenticationException("유효하지 않은 리프레시 토큰입니다.");
        }
    }

    private Map<String, Object> buildTokenResponse(String accessToken) {
        return Map.of(
            "accessToken", accessToken,
            "expiresIn", jwtTokenProvider.getAccessTokenExpirationSeconds(),
            "tokenType", "Bearer"
        );
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), refreshToken)
            .httpOnly(true)
            .secure(jwtProperties.isRefreshCookieSecure())
            .sameSite(jwtProperties.getRefreshCookieSameSite())
            .path(jwtProperties.getRefreshCookiePath())
            .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()))
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtProperties.getRefreshCookieName(), "")
            .httpOnly(true)
            .secure(jwtProperties.isRefreshCookieSecure())
            .sameSite(jwtProperties.getRefreshCookieSameSite())
            .path(jwtProperties.getRefreshCookiePath())
            .maxAge(Duration.ZERO)
            .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        return Arrays.stream(cookies)
            .filter(cookie -> jwtProperties.getRefreshCookieName().equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
