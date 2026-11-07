package com.paymentsystem.otpservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI serviceOpenApi() {
        return new OpenAPI()
            .info(new Info().title("KDNPay - OTP Service").version("1.0"))
            .servers(List.of(new Server().url("/docs-api/otp")))
            .components(new Components().addSecuritySchemes("InternalServiceToken",
                new SecurityScheme().type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER).name("X-Internal-Service-Token")))
            .addSecurityItem(new SecurityRequirement().addList("InternalServiceToken"));
    }

    @Bean
    OpenApiCustomizer safeDocumentation() {
        return api -> {
            // Annotations may contain old credential examples; never publish these in documentation.
            if (api.getPaths() != null) api.getPaths().values().forEach(item ->
                item.readOperations().forEach(operation -> {
                    operation.setDescription(null);
                    if (operation.getParameters() != null) operation.setParameters(
                        operation.getParameters().stream().filter(parameter ->
                            !"Authorization".equalsIgnoreCase(parameter.getName()) &&
                            !"X-Internal-Service-Token".equalsIgnoreCase(parameter.getName())).toList());
                }));
        };
    }
}
