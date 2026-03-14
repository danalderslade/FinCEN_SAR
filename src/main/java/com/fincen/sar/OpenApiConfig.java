package com.fincen.sar;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sarOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FinCEN SAR Filing API")
                        .version("1.0.0")
                        .description("REST API for managing FinCEN Suspicious Activity Reports (SAR) — "
                                + "supports the full BSA XML Schema 2.0 lifecycle including batch management, "
                                + "activity creation, party management, and filing workflow.")
                        .contact(new Contact().name("FinCEN SAR Team")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from POST /auth/login")));
    }
}
