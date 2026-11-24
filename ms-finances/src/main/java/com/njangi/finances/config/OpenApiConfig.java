package com.njangi.finances.config;

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
                        .title("Njangi — Microservice Finances (Cotisations, Pots, Paiements & Pénalités)")
                        .version("1.0.0")
                        .description("API financière consolidée ACID : multi-cotisations, tours de pot rotatifs, paiements Cash avec reçu signé, Mobile Money MTN/Orange idempotents, barèmes et sanctions disciplinaires")
                        .contact(new Contact()
                                .name("Équipe Njangi Platform")
                                .email("contact@njangi.cm"))
                        .license(new License()
                                .name("Propriétaire")
                                .url("https://njangi.cm")));
    }
}
