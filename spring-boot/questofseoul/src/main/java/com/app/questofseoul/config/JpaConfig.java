package com.app.questofseoul.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.app.questofseoul.repository")
public class JpaConfig {
    // PostGIS dialect is configured in application.properties
}
