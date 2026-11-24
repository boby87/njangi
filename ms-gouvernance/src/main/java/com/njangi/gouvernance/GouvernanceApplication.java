package com.njangi.gouvernance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GouvernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GouvernanceApplication.class, args);
    }
}
