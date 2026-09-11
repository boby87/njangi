package com.njangi.membres.event;

import com.njangi.membres.entity.Membre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembreEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishMembreInscrit(Membre membre) {
        Map<String, Object> event = Map.of(
                "type", "MEMBRE_INSCRIT",
                "membreId", membre.getId().toString(),
                "nom", membre.getNom(),
                "prenom", membre.getPrenom(),
                "email", membre.getEmail(),
                "telephone", membre.getTelephone(),
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send("membre.inscrit", membre.getId().toString(), event);
        log.info("Evenement MEMBRE_INSCRIT publie pour : {}", membre.getId());
    }
}
