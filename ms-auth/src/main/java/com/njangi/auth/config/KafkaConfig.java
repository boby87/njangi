package com.njangi.auth.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String AUTH_EVENTS_TOPIC = "auth.events";

    @Bean
    public NewTopic authEventsTopic() {
        return TopicBuilder.name(AUTH_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
