package com.app.questofseoul.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai-server")
public record AiServerProperties(
    String baseUrl,
    boolean enabled
) {
    public AiServerProperties(String baseUrl, boolean enabled) {
        this.baseUrl = baseUrl != null ? baseUrl : "";
        this.enabled = enabled;
    }
}
