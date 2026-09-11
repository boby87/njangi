package com.njangi.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.Getter;
import lombok.Setter;

@Configuration
@EnableConfigurationProperties(RoutingConfig.NjangiServicesConfig.class)
public class RoutingConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @ConfigurationProperties(prefix = "njangi.services")
    @Getter
    @Setter
    public static class NjangiServicesConfig {
        private String authUrl;
        private String membresUrl;
        private String groupesUrl;
        private String reunionsUrl;
        private String cotisationsUrl;
        private String paiementsUrl;
        private String penalitesUrl;
        private String notificationsUrl;
        private String statistiquesUrl;
    }
}
