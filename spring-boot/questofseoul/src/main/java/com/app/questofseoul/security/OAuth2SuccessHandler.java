package com.app.questofseoul.security;

import com.app.questofseoul.config.JwtProperties;
import com.app.questofseoul.domain.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

/**
 * OAuth2 로그인 성공 시 인증 객체에서 userId를 추출하여 JWT 발급 후 프론트엔드로 리다이렉트.
 * 액세스 토큰은 redirect query로, 리프레시 토큰은 HttpOnly 쿠키로 설정합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException {
        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            redirectToFailure(response);
            return;
        }
        Object userIdAttr = oauth2User.getAttribute(CustomOAuth2UserService.USER_ID_ATTR);
        if (userIdAttr == null) {
            log.warn("OAuth2 user missing userId attribute");
            redirectToFailure(response);
            return;
        }
        try {
            UUID userId = UUID.fromString(userIdAttr.toString());
            Object roleAttr = oauth2User.getAttribute(CustomOAuth2UserService.USER_ROLE_ATTR);
            UserRole role = SecurityRoleUtils.parseRole(roleAttr);
            String accessToken = jwtTokenProvider.generateAccessToken(userId, role);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
            setRefreshTokenCookie(response, refreshToken);
            String redirectUrl = frontendUrl + "/login?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid userId in OAuth2 attributes: {}", userIdAttr);
            redirectToFailure(response);
        }
    }

    private void redirectToFailure(HttpServletResponse response) throws IOException {
        response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
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
}
