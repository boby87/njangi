package com.njangi.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(RoutingConfig.NjangiServicesConfig.class)
public class RoutingConfig {

    @Bean
    @LoadBalanced
    @Qualifier("loadBalancedRestTemplate")
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Qualifier("directRestTemplate")
    public RestTemplate directRestTemplate() {
        return new RestTemplate();
    }

    @ConfigurationProperties(prefix = "njangi.services")
    public static class NjangiServicesConfig {
        private String identiteUrl = "lb://ms-identite";
        private String gouvernanceUrl = "lb://ms-gouvernance";
        private String financesUrl = "lb://ms-finances";
        private String engagementUrl = "lb://ms-engagement";

        public String getIdentiteUrl() { return identiteUrl; }
        public void setIdentiteUrl(String identiteUrl) { this.identiteUrl = identiteUrl; }

        public String getGouvernanceUrl() { return gouvernanceUrl; }
        public void setGouvernanceUrl(String gouvernanceUrl) { this.gouvernanceUrl = gouvernanceUrl; }

        public String getFinancesUrl() { return financesUrl; }
        public void setFinancesUrl(String financesUrl) { this.financesUrl = financesUrl; }

        public String getEngagementUrl() { return engagementUrl; }
        public void setEngagementUrl(String engagementUrl) { this.engagementUrl = engagementUrl; }
    }
}
