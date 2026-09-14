package com.njangi.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(RoutingConfig.NjangiServicesConfig.class)
public class RoutingConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @ConfigurationProperties(prefix = "njangi.services")
    public static class NjangiServicesConfig {
        private String authUrl = "http://localhost:8001";
        private String membresUrl = "http://localhost:8002";
        private String groupesUrl = "http://localhost:8003";
        private String reunionsUrl = "http://localhost:8004";
        private String cotisationsUrl = "http://localhost:8005";
        private String paiementsUrl = "http://localhost:8006";
        private String penalitesUrl = "http://localhost:8007";
        private String notificationsUrl = "http://localhost:8008";
        private String statistiquesUrl = "http://localhost:8009";

        public String getAuthUrl() { return authUrl; }
        public void setAuthUrl(String authUrl) { this.authUrl = authUrl; }

        public String getMembresUrl() { return membresUrl; }
        public void setMembresUrl(String membresUrl) { this.membresUrl = membresUrl; }

        public String getGroupesUrl() { return groupesUrl; }
        public void setGroupesUrl(String groupesUrl) { this.groupesUrl = groupesUrl; }

        public String getReunionsUrl() { return reunionsUrl; }
        public void setReunionsUrl(String reunionsUrl) { this.reunionsUrl = reunionsUrl; }

        public String getCotisationsUrl() { return cotisationsUrl; }
        public void setCotisationsUrl(String cotisationsUrl) { this.cotisationsUrl = cotisationsUrl; }

        public String getPaiementsUrl() { return paiementsUrl; }
        public void setPaiementsUrl(String paiementsUrl) { this.paiementsUrl = paiementsUrl; }

        public String getPenalitesUrl() { return penalitesUrl; }
        public void setPenalitesUrl(String penalitesUrl) { this.penalitesUrl = penalitesUrl; }

        public String getNotificationsUrl() { return notificationsUrl; }
        public void setNotificationsUrl(String notificationsUrl) { this.notificationsUrl = notificationsUrl; }

        public String getStatistiquesUrl() { return statistiquesUrl; }
        public void setStatistiquesUrl(String statistiquesUrl) { this.statistiquesUrl = statistiquesUrl; }
    }
}
