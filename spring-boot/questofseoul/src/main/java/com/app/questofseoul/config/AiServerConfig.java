package com.app.questofseoul.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {
}
