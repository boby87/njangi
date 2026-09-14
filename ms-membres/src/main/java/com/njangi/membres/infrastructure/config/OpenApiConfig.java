package com.njangi.membres.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI membresOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Njangi Platform — Microservice Membres")
                        .description("API REST pour la gestion des profils, des rôles et des adhésions multi-groupes selon l'architecture Hexagonale & DDD.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Équipe Njangi")
                                .email("contact@njangi.cm")
                                .url("https://njangi.cm"))
                        .license(new License()
                                .name("Tous droits réservés - Njangi Cameroun")))
                .servers(List.of(
                        new Server().url("http://localhost:9090").description("Passerelle API Gateway (Recommandé)"),
                        new Server().url("http://localhost:8002").description("Direct ms-membres (Dev local)")
                ));
    }
}
