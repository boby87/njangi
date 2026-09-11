package com.njangi.paiements.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_COTISATION_PAYEE = "cotisation.payee";
    public static final String TOPIC_POT_VERSE = "pot.verse";

    @Bean
    public NewTopic cotisationPayeeTopic() {
        return TopicBuilder.name(TOPIC_COTISATION_PAYEE)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic potVerseTopic() {
        return TopicBuilder.name(TOPIC_POT_VERSE)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
