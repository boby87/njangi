package com.njangi.statistiques;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StatistiquesApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatistiquesApplication.class, args);
    }
}
