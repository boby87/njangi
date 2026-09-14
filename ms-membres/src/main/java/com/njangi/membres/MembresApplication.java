package com.njangi.membres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MembresApplication {
    public static void main(String[] args) {
        SpringApplication.run(MembresApplication.class, args);
    }
}
