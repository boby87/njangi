package com.njangi.statistiques.kafka;

import com.njangi.statistiques.entity.StatistiqueGroupe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Publie des snapshots de statistiques calculées.
 * Utile si d'autres microservices (ex. ms-notifications) consomment ces agrégats.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatistiqueEventPublisher {

    private static final String TOPIC_STATISTIQUE_CALCULEE = "statistique.calculee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStatistiqueCalculee(StatistiqueGroupe stat) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "STATISTIQUE_CALCULEE");
        event.put("statistiqueId", stat.getId().toString());
        event.put("groupeId", stat.getGroupeId().toString());
        event.put("sessionId", stat.getSessionId().toString());
        event.put("totalCollecte", stat.getTotalCollecte());
        event.put("nbMembres", stat.getNbMembres());
        event.put("tauxParticipation", stat.getTauxParticipation());
        event.put("tauxPresence", stat.getTauxPresence());
        event.put("calculeLe", stat.getCalculeLe() != null ? stat.getCalculeLe().toString() : null);

        kafkaTemplate.send(TOPIC_STATISTIQUE_CALCULEE, stat.getGroupeId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication de {} pour groupeId={}",
                                TOPIC_STATISTIQUE_CALCULEE, stat.getGroupeId(), ex);
                    } else {
                        log.info("Événement {} publié — groupeId={} sessionId={}",
                                TOPIC_STATISTIQUE_CALCULEE, stat.getGroupeId(), stat.getSessionId());
                    }
                });
    }
}
