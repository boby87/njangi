package com.njangi.engagement.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_IDENTITE_EVENTS = "identite.events";
    public static final String TOPIC_GOUVERNANCE_EVENTS = "gouvernance.events";
    public static final String TOPIC_FINANCES_EVENTS = "finances.events";
    public static final String TOPIC_ENGAGEMENT_EVENTS = "engagement.events";

    @Bean
    public NewTopic identiteEventsTopic() {
        return TopicBuilder.name(TOPIC_IDENTITE_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic gouvernanceEventsTopic() {
        return TopicBuilder.name(TOPIC_GOUVERNANCE_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic financesEventsTopic() {
        return TopicBuilder.name(TOPIC_FINANCES_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic engagementEventsTopic() {
        return TopicBuilder.name(TOPIC_ENGAGEMENT_EVENTS).partitions(3).replicas(1).build();
    }
}
