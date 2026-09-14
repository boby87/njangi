package com.njangi.reunions.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_REUNION_EVENTS = "reunion.events";
    public static final String TOPIC_REUNION_TERMINEE = "reunion.terminee";

    @Bean
    public NewTopic reunionEventsTopic() {
        return TopicBuilder.name(TOPIC_REUNION_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic reunionTermineeTopic() {
        return TopicBuilder.name(TOPIC_REUNION_TERMINEE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
