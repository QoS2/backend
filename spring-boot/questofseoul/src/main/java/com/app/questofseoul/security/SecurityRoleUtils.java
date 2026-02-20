package com.app.questofseoul.security;

import com.app.questofseoul.domain.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Locale;

public final class SecurityRoleUtils {

    private SecurityRoleUtils() {}

    public static UserRole parseRole(Object rawRole) {
        if (rawRole == null) {
            return UserRole.USER;
        }
        String value = String.valueOf(rawRole).trim();
        if (value.isEmpty()) {
            return UserRole.USER;
        }
        try {
            return UserRole.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return UserRole.USER;
        }
    }

    public static List<GrantedAuthority> toAuthorities(UserRole role) {
        if (role == UserRole.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
