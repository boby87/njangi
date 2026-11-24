package com.njangi.engagement.config;

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
                        .title("Njangi — Microservice Engagement (Notifications & Statistiques)")
                        .version("1.0.0")
                        .description("API unifiée d'envoi de notifications multi-canaux (Push FCM, SMS, Email, In-App), d'agrégation statistique et de bilans financiers Njangi")
                        .contact(new Contact()
                                .name("Équipe Njangi Platform")
                                .email("contact@njangi.cm"))
                        .license(new License()
                                .name("Propriétaire")
                                .url("https://njangi.cm")));
    }
}
