package com.njangi.statistiques.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic groupeCree() {
        return TopicBuilder.name("groupe.cree").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic membreInscrit() {
        return TopicBuilder.name("membre.inscrit").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic cotisationPayee() {
        return TopicBuilder.name("cotisation.payee").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic potVerse() {
        return TopicBuilder.name("pot.verse").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic penaliteAppliquee() {
        return TopicBuilder.name("penalite.appliquee").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic reunionTerminee() {
        return TopicBuilder.name("reunion.terminee").partitions(3).replicas(1).build();
    }
}
