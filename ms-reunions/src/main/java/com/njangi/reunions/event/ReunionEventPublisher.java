package com.njangi.reunions.event;

import com.njangi.reunions.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class ReunionEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ReunionEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ReunionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierReunionCreee(UUID reunionId, UUID groupeId, String titre, LocalDateTime dateReunion) {
        ReunionEvent event = ReunionEvent.of(
                "REUNION_CREEE",
                reunionId,
                groupeId,
                Map.of(
                        "titre", titre,
                        "dateReunion", dateReunion != null ? dateReunion.toString() : ""
                )
        );
        envoyer(KafkaConfig.TOPIC_REUNION_EVENTS, reunionId.toString(), event);
    }

    public void publierReunionDemarree(UUID reunionId, UUID groupeId, String titre) {
        ReunionEvent event = ReunionEvent.of(
                "REUNION_DEMARREE",
                reunionId,
                groupeId,
                Map.of("titre", titre)
        );
        envoyer(KafkaConfig.TOPIC_REUNION_EVENTS, reunionId.toString(), event);
    }

    public void publierReunionTerminee(UUID reunionId, UUID groupeId, String titre, String compteRendu) {
        ReunionEvent event = ReunionEvent.of(
                "REUNION_TERMINEE",
                reunionId,
                groupeId,
                Map.of(
                        "titre", titre,
                        "compteRendu", compteRendu != null ? compteRendu : ""
                )
        );
        envoyer(KafkaConfig.TOPIC_REUNION_TERMINEE, reunionId.toString(), event);
        envoyer(KafkaConfig.TOPIC_REUNION_EVENTS, reunionId.toString(), event);
    }

    public void publierPresenceEnregistree(UUID reunionId, UUID membreId, String statut) {
        ReunionEvent event = ReunionEvent.of(
                "PRESENCE_ENREGISTREE",
                reunionId,
                null,
                Map.of(
                        "membreId", membreId.toString(),
                        "statut", statut
                )
        );
        envoyer(KafkaConfig.TOPIC_REUNION_EVENTS, reunionId.toString(), event);
    }

    private void envoyer(String topic, String key, Object payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec d'envoi de l'evenement sur le topic '{}' (cle={}) : {}", topic, key, ex.getMessage());
                    } else {
                        log.info("Evenement publie sur topic='{}' cle='{}' partition={} offset={}",
                                topic, key,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
