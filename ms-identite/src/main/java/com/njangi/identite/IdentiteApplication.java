package com.njangi.identite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class IdentiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentiteApplication.class, args);
    }
}
