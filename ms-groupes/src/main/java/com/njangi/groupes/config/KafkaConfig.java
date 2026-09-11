package com.njangi.groupes.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic groupeCreeTopic() {
        return TopicBuilder.name("groupe.cree")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
