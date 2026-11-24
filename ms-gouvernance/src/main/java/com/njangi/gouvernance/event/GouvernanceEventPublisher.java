package com.njangi.gouvernance.event;

import com.njangi.gouvernance.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GouvernanceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GouvernanceEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GouvernanceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierGroupeCree(UUID groupeId, String nom, UUID createurMembreId, String typeSiege) {
        DomainEvent event = DomainEvent.of(
                "GROUPE_CREE",
                groupeId,
                createurMembreId,
                Map.of(
                        "nom", nom,
                        "createurMembreId", createurMembreId.toString(),
                        "typeSiege", typeSiege
                )
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierMembreAjoute(UUID groupeId, UUID membreId, String role) {
        DomainEvent event = DomainEvent.of(
                "MEMBRE_AJOUTE",
                groupeId,
                membreId,
                Map.of(
                        "membreId", membreId.toString(),
                        "role", role
                )
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierSessionPlanifiee(UUID groupeId, UUID sessionId, String libelle) {
        DomainEvent event = DomainEvent.of(
                "SESSION_PLANIFIEE",
                groupeId,
                null,
                Map.of(
                        "sessionId", sessionId.toString(),
                        "libelle", libelle
                )
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierSessionDemarree(UUID groupeId, UUID sessionId) {
        DomainEvent event = DomainEvent.of(
                "SESSION_DEMARREE",
                groupeId,
                null,
                Map.of("sessionId", sessionId.toString())
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierSessionCloturee(UUID groupeId, UUID sessionId) {
        DomainEvent event = DomainEvent.of(
                "SESSION_CLOTUREE",
                groupeId,
                null,
                Map.of("sessionId", sessionId.toString())
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierBureauElu(UUID groupeId, UUID presidentId, UUID tresorierId,
                                 UUID secretaireId, UUID createurARetrograder) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("presidentId", presidentId.toString());
        if (tresorierId != null) {
            payload.put("tresorierId", tresorierId.toString());
        }
        if (secretaireId != null) {
            payload.put("secretaireId", secretaireId.toString());
        }
        if (createurARetrograder != null) {
            payload.put("createurRetrogradeId", createurARetrograder.toString());
        }

        DomainEvent event = DomainEvent.of(
                "BUREAU_ELU",
                groupeId,
                presidentId,
                payload
        );
        envoyer(groupeId.toString(), event);
    }

    public void publierReunionPlanifiee(UUID reunionId, UUID groupeId, String titre, LocalDateTime dateReunion) {
        DomainEvent event = DomainEvent.of(
                "REUNION_PLANIFIEE",
                groupeId,
                null,
                Map.of(
                        "reunionId", reunionId.toString(),
                        "titre", titre,
                        "dateReunion", dateReunion != null ? dateReunion.toString() : ""
                )
        );
        envoyer(clePartition(groupeId, reunionId), event);
    }

    public void publierReunionDemarree(UUID reunionId, UUID groupeId, String titre) {
        DomainEvent event = DomainEvent.of(
                "REUNION_DEMARREE",
                groupeId,
                null,
                Map.of(
                        "reunionId", reunionId.toString(),
                        "titre", titre
                )
        );
        envoyer(clePartition(groupeId, reunionId), event);
    }

    public void publierReunionTerminee(UUID reunionId, UUID groupeId, String titre, String compteRendu) {
        DomainEvent event = DomainEvent.of(
                "REUNION_TERMINEE",
                groupeId,
                null,
                Map.of(
                        "reunionId", reunionId.toString(),
                        "titre", titre,
                        "compteRendu", compteRendu != null ? compteRendu : ""
                )
        );
        envoyer(clePartition(groupeId, reunionId), event);
    }

    public void publierPresenceEnregistree(UUID reunionId, UUID groupeId, UUID membreId, String statut) {
        DomainEvent event = DomainEvent.of(
                "PRESENCE_ENREGISTREE",
                groupeId,
                membreId,
                Map.of(
                        "reunionId", reunionId.toString(),
                        "membreId", membreId.toString(),
                        "statut", statut
                )
        );
        envoyer(clePartition(groupeId, reunionId), event);
    }

    private String clePartition(UUID groupeId, UUID fallbackId) {
        if (groupeId != null) {
            return groupeId.toString();
        }
        return fallbackId != null ? fallbackId.toString() : UUID.randomUUID().toString();
    }

    private void envoyer(String key, DomainEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_GOUVERNANCE_EVENTS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Échec d'envoi de l'événement '{}' sur topic='{}' (clé={}) : {}",
                                event.eventType(), KafkaConfig.TOPIC_GOUVERNANCE_EVENTS, key, ex.getMessage());
                    } else if (result != null && result.getRecordMetadata() != null) {
                        log.info("Événement '{}' publié sur topic='{}' clé='{}' partition={} offset={}",
                                event.eventType(), KafkaConfig.TOPIC_GOUVERNANCE_EVENTS, key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
