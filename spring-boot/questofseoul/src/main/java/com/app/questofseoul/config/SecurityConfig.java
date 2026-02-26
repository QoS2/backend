package com.app.questofseoul.config;

import com.app.questofseoul.security.CustomOAuth2UserService;
import com.app.questofseoul.security.CustomOidcUserService;
import com.app.questofseoul.security.JwtAuthenticationFilter;
import com.app.questofseoul.security.OAuth2PrincipalFilter;
import com.app.questofseoul.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2PrincipalFilter oAuth2PrincipalFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtProperties jwtProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/oauth2/**", "/login/**").permitAll()
                .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/tours/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/photo-spots", "/api/v1/photo-spots/*").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/steps/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/spots/*/guide").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/spots/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/v1/tour-runs/**", "/api/v1/collections/**").authenticated()
                .requestMatchers("/api/v1/chat-sessions/**").authenticated()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/upload/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")
                )
                .defaultAccessDeniedHandlerFor(
                    (request, response, accessDeniedException) ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN),
                    new AntPathRequestMatcher("/api/**")
                )
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler(oauth2FailureHandler())
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .deleteCookies("JSESSIONID", jwtProperties.getRefreshCookieName())
                .logoutSuccessUrl(frontendUrl + "/login")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(oAuth2PrincipalFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private SimpleUrlAuthenticationFailureHandler oauth2FailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler(frontendUrl + "/login?error=oauth_failed");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
