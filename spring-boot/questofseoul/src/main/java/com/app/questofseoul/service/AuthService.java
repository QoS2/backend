package com.app.questofseoul.service;

import com.app.questofseoul.config.AuthRoleProperties;
import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.domain.enums.UserRole;
import com.app.questofseoul.exception.AuthenticationException;
import com.app.questofseoul.exception.DuplicateResourceException;
import com.app.questofseoul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRoleProperties authRoleProperties;

    @Transactional
    public UUID getOrCreateUserFromGoogle(String googleSub, String email, String name) {
        User user = userRepository.findByGoogleSub(googleSub)
            .orElseGet(() -> {
                log.info("Creating new user with googleSub: {}", googleSub);
                return userRepository.save(User.create(googleSub, email, name));
            });

        if (email != null && !email.equals(user.getEmail()) ||
            name != null && !name.equals(user.getNickname())) {
            user.updateProfile(email, name);
            userRepository.save(user);
        }

        return user.getId();
    }

    @Transactional
    public UUID register(String email, String password, String nickname) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("이메일", email);
        }
        String encoded = passwordEncoder.encode(password);
        User user = userRepository.save(User.createWithPassword(email, nickname, encoded));
        log.info("Registered user: {}", user.getEmail());
        return user.getId();
    }

    public UUID login(String email, String password) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다."));
        if (!user.hasPassword()) {
            throw new AuthenticationException("이 계정은 비밀번호 로그인이 아닌 Google 로그인을 사용합니다.");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
        return user.getId();
    }

    @Transactional(readOnly = true)
    public UserRole resolveRole(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("인증되지 않은 사용자입니다"));
        return resolveRole(user);
    }

    public UUID getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }

        throw new com.app.questofseoul.exception.AuthenticationException("인증되지 않은 사용자입니다");
    }

    private UserRole resolveRole(User user) {
        Set<String> adminIds = authRoleProperties.getAdminUserIds().stream()
                .map(this::normalize)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toSet());
        Set<String> adminEmails = authRoleProperties.getAdminEmails().stream()
                .map(this::normalize)
                .filter(v -> !v.isEmpty())
                .collect(Collectors.toSet());

        // 하위 호환: allowlist가 비어 있으면 기존 동작(인증 사용자 = ADMIN) 유지
        if (adminIds.isEmpty() && adminEmails.isEmpty()) {
            return UserRole.ADMIN;
        }

        String userId = normalize(user.getId() != null ? user.getId().toString() : null);
        String email = normalize(user.getEmail());

        if ((!userId.isEmpty() && adminIds.contains(userId))
                || (!email.isEmpty() && adminEmails.contains(email))) {
            return UserRole.ADMIN;
        }
        return UserRole.USER;
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
