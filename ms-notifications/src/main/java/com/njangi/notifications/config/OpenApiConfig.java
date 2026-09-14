package com.njangi.notifications.config;

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
                        .title("Njangi — Microservice Notifications")
                        .version("1.0.0")
                        .description("API d'envoi et de gestion des notifications multi-canaux (Push FCM, SMS fallback, Email, In-App)")
                        .contact(new Contact()
                                .name("Équipe Njangi Platform")
                                .email("contact@njangi.cm"))
                        .license(new License()
                                .name("Propriétaire")
                                .url("https://njangi.cm")));
    }
}
