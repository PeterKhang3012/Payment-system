package com.paymentsystem.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI config.
 * Swagger UI: http://localhost:8081/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("User Service API")
                .description("API Documentation — User Service\n\n"
                           + "Bao gom: Xac thuc JWT, thong tin sinh vien, tra cuu va cap nhat hoc phi.\n\n"
                           + "Huong dan test:\n"
                           + "1. Goi POST /api/auth/login voi username/password mau.\n"
                           + "2. Copy truong token trong response.\n"
                           + "3. Click Authorize → nhap Bearer <token>.\n\n"
                           + "Tai khoan mau: nguyenvana / Student@123 | tranthib / Student@123 | lequanghung / Student@123")
                .version("1.0.0")
                .contact(new Contact().name("Thanh vien 2").email("tv2@university.edu.vn")))
            .servers(List.of(new Server().url("http://localhost:8081").description("Local")))
            .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
            .components(new Components()
                .addSecuritySchemes("BearerAuth", new SecurityScheme()
                    .name("BearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Nhap JWT token tu response cua POST /api/auth/login")));
    }
}
