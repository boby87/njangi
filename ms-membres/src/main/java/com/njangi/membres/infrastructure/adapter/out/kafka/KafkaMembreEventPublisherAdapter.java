package com.njangi.membres.infrastructure.adapter.out.kafka;

import com.njangi.membres.domain.event.MembreInscritEvent;
import com.njangi.membres.domain.event.RoleAssigneeEvent;
import com.njangi.membres.domain.port.out.MembreEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class KafkaMembreEventPublisherAdapter implements MembreEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaMembreEventPublisherAdapter.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaMembreEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publierMembreInscrit(MembreInscritEvent event) {
        Map<String, Object> payload = Map.of(
                "type", "MEMBRE_INSCRIT",
                "membreId", event.membreId().toString(),
                "authUtilisateurId", event.authUtilisateurId().toString(),
                "nom", event.nom(),
                "prenom", event.prenom(),
                "email", event.email(),
                "telephone", event.telephone(),
                "timestamp", event.survenuLe().toString()
        );
        kafkaTemplate.send("membre.inscrit", event.membreId().toString(), payload);
        log.info("Evenement MEMBRE_INSCRIT publie pour : {}", event.membreId());
    }

    @Override
    public void publierRoleAssignee(RoleAssigneeEvent event) {
        Map<String, Object> payload = Map.of(
                "type", "ROLE_ASSIGNE",
                "adhesionId", event.adhesionId().toString(),
                "utilisateurId", event.utilisateurId().toString(),
                "groupeId", event.groupeId().toString(),
                "role", event.nouveauRole().name(),
                "timestamp", event.survenuLe().toString()
        );
        kafkaTemplate.send("membre.role_assigne", event.utilisateurId().toString(), payload);
        log.info("Evenement ROLE_ASSIGNE publie pour : {} (Role: {})", event.utilisateurId(), event.nouveauRole());
    }
}
