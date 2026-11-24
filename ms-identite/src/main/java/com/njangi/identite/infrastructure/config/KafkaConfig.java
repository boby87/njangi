package com.njangi.identite.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String IDENTITE_EVENTS_TOPIC = "identite.events";

    @Bean
    public NewTopic identiteEventsTopic() {
        return TopicBuilder.name(IDENTITE_EVENTS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
