package com.app.questofseoul.security;

import com.app.questofseoul.config.JwtProperties;
import com.app.questofseoul.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private static final String ROLE_CLAIM_KEY = "role";
    private static final String TOKEN_TYPE_CLAIM_KEY = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtProperties jwtProperties;

    public String generateAccessToken(UUID userId, UserRole role) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + jwtProperties.getAccessTokenExpirationMs());
        UserRole resolvedRole = role != null ? role : UserRole.USER;

        return Jwts.builder()
            .subject(userId.toString())
            .claim(TOKEN_TYPE_CLAIM_KEY, ACCESS_TOKEN_TYPE)
            .claim(ROLE_CLAIM_KEY, resolvedRole.name())
            .issuedAt(new Date(now))
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(UUID userId) {
        long now = System.currentTimeMillis();
        Date expiry = new Date(now + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
            .subject(userId.toString())
            .claim(TOKEN_TYPE_CLAIM_KEY, REFRESH_TOKEN_TYPE)
            .issuedAt(new Date(now))
            .expiration(expiry)
            .signWith(getSigningKey())
            .compact();
    }

    public UUID getUserIdFromToken(String token) {
        try {
            Claims payload = parseClaims(token);
            ensureAccessToken(payload);
            return extractUserId(payload);
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            throw e;
        }
    }

    public UserRole getRoleFromToken(String token) {
        try {
            Claims payload = parseClaims(token);
            ensureAccessToken(payload);
            Object rawRole = payload.get(ROLE_CLAIM_KEY);
            if (rawRole == null) {
                return UserRole.USER;
            }
            try {
                return UserRole.valueOf(String.valueOf(rawRole).trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return UserRole.USER;
            }
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT while parsing role: {}", e.getMessage());
            throw e;
        }
    }

    public UUID getUserIdFromRefreshToken(String token) {
        try {
            Claims payload = parseClaims(token);
            ensureRefreshToken(payload);
            return extractUserId(payload);
        } catch (ExpiredJwtException e) {
            log.debug("Refresh JWT expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid refresh JWT: {}", e.getMessage());
            throw e;
        }
    }

    public boolean validateToken(String token) {
        try {
            getUserIdFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            getUserIdFromRefreshToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationMs() / 1000;
    }

    public long getRefreshTokenExpirationSeconds() {
        return jwtProperties.getRefreshTokenExpirationMs() / 1000;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private UUID extractUserId(Claims payload) {
        String userIdStr = payload.getSubject();
        if (userIdStr == null || userIdStr.isBlank()) {
            throw new JwtException("JWT subject (userId) is missing");
        }
        return UUID.fromString(userIdStr);
    }

    private void ensureAccessToken(Claims payload) {
        String tokenType = resolveTokenType(payload);
        if (REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new JwtException("Refresh token cannot be used as access token");
        }
    }

    private void ensureRefreshToken(Claims payload) {
        String tokenType = resolveTokenType(payload);
        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new JwtException("Token is not a refresh token");
        }
    }

    private String resolveTokenType(Claims payload) {
        Object tokenType = payload.get(TOKEN_TYPE_CLAIM_KEY);
        if (tokenType == null) {
            return "";
        }
        return String.valueOf(tokenType).trim().toLowerCase(Locale.ROOT);
    }
}
