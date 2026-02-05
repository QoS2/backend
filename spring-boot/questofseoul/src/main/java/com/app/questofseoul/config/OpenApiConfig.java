package com.app.questofseoul.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI questOfSeoulOpenAPI() {
        String sessionScheme = "sessionAuth";
        String bearerScheme = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Quest of Seoul API")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList(sessionScheme))
            .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
            .components(new Components()
                .addSecuritySchemes(sessionScheme,
                    new SecurityScheme()
                        .name("JSESSIONID")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .description("Google OAuth2 로그인 후 발급되는 세션 쿠키"))
                .addSecuritySchemes(bearerScheme,
                    new SecurityScheme()
                        .name("Authorization")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("POST /api/v1/auth/token 으로 발급받은 JWT 액세스 토큰")));
    }
}
