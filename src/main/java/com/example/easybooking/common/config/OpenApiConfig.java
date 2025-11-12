package com.example.easybooking.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${server-url:http://localhost:8080}") String serverUrl) {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()    // 완전 수식
                        .title("Snapbook API")
                        .version("v1"))
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server() // 완전 수식
                        .url(serverUrl)));
    }
}