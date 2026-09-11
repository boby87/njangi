package com.njangi.penalites.kafka;

import com.njangi.penalites.entity.TypeInfraction;
import com.njangi.penalites.service.PenaliteService;
import com.njangi.penalites.dto.PenaliteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consommateur d'événements Kafka pour ms-penalites.
 * Écoute "cotisation.payee" afin de détecter les retards de paiement
 * et appliquer automatiquement les pénalités correspondantes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PenaliteEventConsumer {

    private final PenaliteService penaliteService;

    /**
     * Déclenché quand une cotisation est enregistrée comme payée.
     * Si le paiement est marqué en retard (champ "enRetard" = true dans l'événement),
     * une pénalité RETARD_PAIEMENT est automatiquement créée.
     */
    @KafkaListener(topics = "cotisation.payee", groupId = "ms-penalites")
    public void onCotisationPayee(Map<String, Object> event) {
        log.info("Événement cotisation.payee reçu : {}", event);
        try {
            boolean enRetard = Boolean.TRUE.equals(event.get("enRetard"));
            if (!enRetard) {
                return; // Pas de retard, pas de pénalité
            }

            UUID membreId  = UUID.fromString((String) event.get("membreId"));
            UUID groupeId  = UUID.fromString((String) event.get("groupeId"));
            UUID sessionId = UUID.fromString((String) event.get("sessionId"));

            log.info("Retard détecté — application d'une pénalité RETARD_PAIEMENT pour membre={}", membreId);

            PenaliteDto dto = new PenaliteDto(
                    null, membreId, groupeId, sessionId,
                    TypeInfraction.RETARD_PAIEMENT,
                    null, // montant résolu depuis la tarification active
                    null,
                    null
            );
            penaliteService.appliquer(dto);

        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'événement cotisation.payee : {}", event, ex);
        }
    }
}
