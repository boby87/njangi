package com.njangi.engagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EngagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EngagementApplication.class, args);
    }
}
