package com.njangi.statistiques.kafka;

import com.njangi.statistiques.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Consommateur d'événements Kafka pour ms-statistiques.
 * Agrège les données de cotisations et de réunions pour mettre à jour les statistiques des groupes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatistiqueEventConsumer {

    private final StatistiqueService statistiqueService;

    /**
     * Déclenché à chaque paiement de cotisation validé.
     * Incrémente le totalCollecte et met à jour le taux de participation.
     */
    @KafkaListener(topics = "cotisation.payee", groupId = "ms-statistiques")
    public void onCotisationPayee(Map<String, Object> event) {
        log.info("Événement cotisation.payee reçu pour agrégation des statistiques");
        try {
            UUID groupeId  = UUID.fromString((String) event.get("groupeId"));
            UUID sessionId = UUID.fromString((String) event.get("sessionId"));

            BigDecimal montant = null;
            Object montantObj = event.get("montant");
            if (montantObj instanceof Number n) {
                montant = BigDecimal.valueOf(n.doubleValue());
            } else if (montantObj instanceof String s) {
                montant = new BigDecimal(s);
            }

            statistiqueService.mettreAJourDepuisEvenement(groupeId, sessionId, montant, false);

        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'événement cotisation.payee pour les statistiques", ex);
        }
    }

    /**
     * Déclenché à la fin d'une réunion.
     * Met à jour le taux de présence.
     */
    @KafkaListener(topics = "reunion.terminee", groupId = "ms-statistiques")
    public void onReunionTerminee(Map<String, Object> event) {
        log.info("Événement reunion.terminee reçu pour agrégation des statistiques");
        try {
            UUID groupeId  = UUID.fromString((String) event.get("groupeId"));
            UUID sessionId = UUID.fromString((String) event.get("sessionId"));

            // Met à jour les statistiques sans incrémenter le montant
            statistiqueService.mettreAJourDepuisEvenement(groupeId, sessionId, null, true);

        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'événement reunion.terminee pour les statistiques", ex);
        }
    }
}
