package com.app.questofseoul.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
            
            Map<String, Object> envProperties = new HashMap<>();
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                // Only add if not already set in environment
                if (environment.getProperty(key) == null) {
                    envProperties.put(key, value);
                }
            });
            
            if (!envProperties.isEmpty()) {
                environment.getPropertySources().addFirst(
                    new MapPropertySource("dotenv", envProperties)
                );
            }
        } catch (Exception e) {
            // Ignore if .env file is missing
        }
    }
}
