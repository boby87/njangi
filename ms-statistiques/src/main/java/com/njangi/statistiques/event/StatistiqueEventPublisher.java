package com.njangi.statistiques.event;

import com.njangi.statistiques.entity.StatistiqueGroupe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StatistiqueEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StatistiqueEventPublisher.class);
    private static final String TOPIC_STATISTIQUE_CALCULEE = "statistique.calculee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public StatistiqueEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStatistiqueCalculee(StatistiqueGroupe stat) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "STATISTIQUE_CALCULEE");
        event.put("statistiqueId", stat.getId() != null ? stat.getId().toString() : null);
        event.put("groupeId", stat.getGroupeId().toString());
        event.put("sessionId", stat.getSessionId().toString());
        event.put("totalCollecte", stat.getTotalCollecte());
        event.put("totalDecaisse", stat.getTotalDecaisse());
        event.put("soldeCaisse", stat.getSoldeCaisse());
        event.put("totalPenalites", stat.getTotalPenalites());
        event.put("totalCash", stat.getTotalCash());
        event.put("totalMomo", stat.getTotalMomo());
        event.put("nbMembres", stat.getNbMembres());
        event.put("nbReunions", stat.getNbReunions());
        event.put("tauxParticipation", stat.getTauxParticipation());
        event.put("tauxPresence", stat.getTauxPresence());
        event.put("calculeLe", stat.getCalculeLe() != null ? stat.getCalculeLe().toString() : null);

        String key = stat.getGroupeId().toString();
        kafkaTemplate.send(TOPIC_STATISTIQUE_CALCULEE, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur publication {} pour groupeId={} : {}", TOPIC_STATISTIQUE_CALCULEE, stat.getGroupeId(), ex.getMessage());
                    } else {
                        log.info("Événement {} publié — groupeId={} sessionId={}", TOPIC_STATISTIQUE_CALCULEE, stat.getGroupeId(), stat.getSessionId());
                    }
                });
    }
}
