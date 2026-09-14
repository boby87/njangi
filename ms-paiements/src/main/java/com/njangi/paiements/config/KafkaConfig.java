package com.njangi.paiements.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_PAIEMENT_EVENTS = "paiement.events";
    public static final String TOPIC_PAIEMENT_VALIDE = "paiement.valide";

    @Bean
    public NewTopic paiementEventsTopic() {
        return TopicBuilder.name(TOPIC_PAIEMENT_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paiementValideTopic() {
        return TopicBuilder.name(TOPIC_PAIEMENT_VALIDE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
