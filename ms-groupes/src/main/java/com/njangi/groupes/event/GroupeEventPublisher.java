package com.njangi.groupes.event;

import com.njangi.groupes.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GroupeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(GroupeEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public GroupeEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierGroupeCree(UUID groupeId, String nom, UUID createurMembreId, String typeSiege) {
        GroupeEvent event = GroupeEvent.of(
                "GROUPE_CREE",
                groupeId,
                Map.of(
                        "nom", nom,
                        "createurMembreId", createurMembreId.toString(),
                        "typeSiege", typeSiege
                )
        );
        envoyer(KafkaConfig.TOPIC_GROUPE_EVENTS, groupeId.toString(), event);
    }

    public void publierMembreRejoint(UUID groupeId, UUID membreId, String role) {
        GroupeEvent event = GroupeEvent.of(
                "MEMBRE_REJOINT",
                groupeId,
                Map.of(
                        "membreId", membreId.toString(),
                        "role", role
                )
        );
        envoyer(KafkaConfig.TOPIC_GROUPE_EVENTS, groupeId.toString(), event);
    }

    public void publierSessionCreee(UUID groupeId, UUID sessionId, String libelle) {
        GroupeEvent event = GroupeEvent.of(
                "SESSION_CREEE",
                groupeId,
                Map.of(
                        "sessionId", sessionId.toString(),
                        "libelle", libelle
                )
        );
        envoyer(KafkaConfig.TOPIC_SESSION_EVENTS, sessionId.toString(), event);
    }

    public void publierSessionDemarree(UUID groupeId, UUID sessionId) {
        GroupeEvent event = GroupeEvent.of(
                "SESSION_DEMARREE",
                groupeId,
                Map.of("sessionId", sessionId.toString())
        );
        envoyer(KafkaConfig.TOPIC_SESSION_EVENTS, sessionId.toString(), event);
    }

    public void publierSessionCloturee(UUID groupeId, UUID sessionId) {
        GroupeEvent event = GroupeEvent.of(
                "SESSION_CLOTUREE",
                groupeId,
                Map.of("sessionId", sessionId.toString())
        );
        envoyer(KafkaConfig.TOPIC_SESSION_EVENTS, sessionId.toString(), event);
    }

    public void publierBureauElu(UUID groupeId, UUID presidentId, UUID tresorierId,
                                 UUID secretaireId, UUID createurARetrograder) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("presidentId", presidentId.toString());
        if (tresorierId != null) payload.put("tresorierId", tresorierId.toString());
        if (secretaireId != null) payload.put("secretaireId", secretaireId.toString());
        if (createurARetrograder != null) payload.put("createurRetrogradeId", createurARetrograder.toString());

        GroupeEvent event = GroupeEvent.of(
                "BUREAU_ELU",
                groupeId,
                payload
        );
        envoyer(KafkaConfig.TOPIC_BUREAU_EVENTS, groupeId.toString(), event);
    }

    private void envoyer(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec d'envoi de l'evenement sur topic='{}' (cle={}) : {}", topic, key, ex.getMessage());
                    } else {
                        log.info("Evenement publie sur topic='{}' cle='{}' partition={} offset={}",
                                topic, key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
