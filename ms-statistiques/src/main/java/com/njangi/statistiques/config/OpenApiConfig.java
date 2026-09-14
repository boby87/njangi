package com.njangi.statistiques.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Njangi — Microservice Statistiques & Reporting")
                        .version("1.0.0")
                        .description("API d'agrégation de données, ratios de performance, bilans de caisse et reporting pour les groupes Njangi")
                        .contact(new Contact()
                                .name("Équipe Njangi Platform")
                                .email("contact@njangi.cm"))
                        .license(new License()
                                .name("Propriétaire")
                                .url("https://njangi.cm")));
    }
}
