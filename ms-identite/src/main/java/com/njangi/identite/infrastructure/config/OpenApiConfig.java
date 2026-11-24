package com.njangi.identite.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI identiteOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Njangi Platform — Microservice Identité (ms-identite)")
                        .description("API REST consolidée pour l'authentification (SSO, OTP E.164, JWT), les profils membres, le KYC et les rôles dynamiques par groupe (Architecture Hexagonale & DDD).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe Architecture Njangi")
                                .email("contact@njangi.cm")
                                .url("https://njangi.cm"))
                        .license(new License()
                                .name("Tous droits réservés - Njangi Cameroun")))
                .servers(List.of(
                        new Server().url("http://localhost:9090").description("Passerelle API Gateway (Recommandé)"),
                        new Server().url("http://localhost:8001").description("Direct ms-identite (Dev local)")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
