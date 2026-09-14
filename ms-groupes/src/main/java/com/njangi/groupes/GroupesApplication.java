package com.njangi.groupes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GroupesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupesApplication.class, args);
    }
}
