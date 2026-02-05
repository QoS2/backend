package com.app.questofseoul.service;

import com.app.questofseoul.domain.entity.User;
import com.app.questofseoul.exception.AuthenticationException;
import com.app.questofseoul.exception.DuplicateResourceException;
import com.app.questofseoul.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public UUID getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UUID) {
            return (UUID) authentication.getPrincipal();
        }

        throw new com.app.questofseoul.exception.AuthenticationException("인증되지 않은 사용자입니다");
    }
}
