package com.njangi.groupes.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_GROUPE_EVENTS = "groupe.events";
    public static final String TOPIC_SESSION_EVENTS = "session.events";
    public static final String TOPIC_BUREAU_EVENTS = "bureau.events";

    @Bean
    public NewTopic groupeEventsTopic() {
        return TopicBuilder.name(TOPIC_GROUPE_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic sessionEventsTopic() {
        return TopicBuilder.name(TOPIC_SESSION_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bureauEventsTopic() {
        return TopicBuilder.name(TOPIC_BUREAU_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
