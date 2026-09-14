package com.njangi.penalites.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_SANCTION_EVENTS = "sanction.events";

    @Bean
    public NewTopic sanctionEventsTopic() {
        return TopicBuilder.name(TOPIC_SANCTION_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
