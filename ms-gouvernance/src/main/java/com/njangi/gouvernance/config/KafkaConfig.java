package com.njangi.gouvernance.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_GOUVERNANCE_EVENTS = "gouvernance.events";

    @Bean
    public NewTopic gouvernanceEventsTopic() {
        return TopicBuilder.name(TOPIC_GOUVERNANCE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
