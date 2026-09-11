package com.njangi.penalites.kafka;

import com.njangi.penalites.entity.Penalite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PenaliteEventPublisher {

    private static final String TOPIC_PENALITE_APPLIQUEE = "penalite.appliquee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPenaliteAppliquee(Penalite penalite) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PENALITE_APPLIQUEE");
        event.put("penaliteId", penalite.getId().toString());
        event.put("membreId", penalite.getMembreId().toString());
        event.put("groupeId", penalite.getGroupeId().toString());
        event.put("sessionId", penalite.getSessionId().toString());
        event.put("typeInfraction", penalite.getTypeInfraction().name());
        event.put("montant", penalite.getMontant());
        event.put("statut", penalite.getStatut().name());
        event.put("creeLe", penalite.getCreeLe() != null ? penalite.getCreeLe().toString() : null);

        kafkaTemplate.send(TOPIC_PENALITE_APPLIQUEE, penalite.getGroupeId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication de {} pour penaliteId={}",
                                TOPIC_PENALITE_APPLIQUEE, penalite.getId(), ex);
                    } else {
                        log.info("Événement {} publié — penaliteId={}", TOPIC_PENALITE_APPLIQUEE, penalite.getId());
                    }
                });
    }
}
