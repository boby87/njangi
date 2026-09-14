package com.njangi.penalites.kafka;

import com.njangi.penalites.dto.InfligerPenaliteRequest;
import com.njangi.penalites.entity.TypeInfraction;
import com.njangi.penalites.service.PenaliteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class PenaliteEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PenaliteEventConsumer.class);
    private final PenaliteService penaliteService;

    public PenaliteEventConsumer(PenaliteService penaliteService) {
        this.penaliteService = penaliteService;
    }

    @KafkaListener(topics = "cotisation.events", groupId = "ms-penalites-group")
    public void onCotisationEvent(Map<String, Object> event) {
        log.info("Evenement cotisation recu dans ms-penalites : {}", event);
        try {
            String type = (String) event.get("type");
            boolean enRetard = Boolean.TRUE.equals(event.get("enRetard")) || "cotisation.en_retard".equals(type);

            if (!enRetard) {
                return;
            }

            if (event.get("membreId") == null || event.get("groupeId") == null) {
                return;
            }

            UUID membreId = UUID.fromString((String) event.get("membreId"));
            UUID groupeId = UUID.fromString((String) event.get("groupeId"));
            UUID sessionId = event.get("sessionId") != null ? UUID.fromString((String) event.get("sessionId")) : UUID.randomUUID();

            log.info("Application automatique d'une pénalité RETARD_PAIEMENT pour membre={}", membreId);

            InfligerPenaliteRequest request = new InfligerPenaliteRequest(
                    membreId,
                    groupeId,
                    sessionId,
                    null,
                    TypeInfraction.RETARD_PAIEMENT,
                    null, // Résolu via barème du groupe
                    "Pénalité automatique suite à retard de cotisation"
            );
            penaliteService.infligerPenalite(request);

        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'événement cotisation : {}", event, ex);
        }
    }
}
