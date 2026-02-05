package com.app.questofseoul.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@Slf4j
public class OAuth2PrincipalFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OAuth2User oauth2User) {
            Object userIdAttr = oauth2User.getAttribute(CustomOAuth2UserService.USER_ID_ATTR);
            if (userIdAttr != null) {
                try {
                    UUID userId = UUID.fromString(userIdAttr.toString());
                    var newAuth = new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid userId in OAuth2 attributes: {}", userIdAttr);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
