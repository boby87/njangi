package com.njangi.finances.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_FINANCES_EVENTS = "finances.events";

    @Bean
    public NewTopic financesEventsTopic() {
        return TopicBuilder.name(TOPIC_FINANCES_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
