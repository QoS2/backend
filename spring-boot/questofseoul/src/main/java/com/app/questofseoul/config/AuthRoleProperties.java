package com.app.questofseoul.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthRoleProperties {

    /**
     * ADMIN 권한 이메일 allowlist.
     * 예: app.auth.admin-emails=admin@example.com,ops@example.com
     */
    private List<String> adminEmails = new ArrayList<>();

    /**
     * ADMIN 권한 userId(UUID) allowlist.
     * 예: app.auth.admin-user-ids=550e8400-e29b-41d4-a716-446655440000
     */
    private List<String> adminUserIds = new ArrayList<>();
}
