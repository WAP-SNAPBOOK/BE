package com.example.easybooking.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

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
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Snapbook API")
                        .version("v1"))
                .servers(List.of(new io.swagger.v3.oas.models.servers.Server()
                        .url(serverUrl)));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // ArgumentResolver로 처리되는 파라미터 필터링
            if (operation.getParameters() != null) {
                List<Parameter> filteredParams = operation.getParameters().stream()
                        .filter(param -> {
                            String paramName = param.getName();
                            // RequireTempUser, RequireAuthenticatedUser로 처리되는 파라미터 숨기기
                            return !paramName.equals("tempUser")
                                    && !paramName.equals("authenticatedUser") && !paramName.equals("user");
                        })
                        .collect(Collectors.toList());
                operation.setParameters(filteredParams);
            }
            return operation;
        };
    }
}