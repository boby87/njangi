package com.njangi.statistiques.event;

import com.njangi.statistiques.service.StatistiqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class StatistiqueEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(StatistiqueEventConsumer.class);

    private final StatistiqueService statistiqueService;

    public StatistiqueEventConsumer(StatistiqueService statistiqueService) {
        this.statistiqueService = statistiqueService;
    }

    @KafkaListener(topics = "cotisation.payee", groupId = "ms-statistiques-group")
    public void onCotisationPayee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka cotisation.payee reçu pour calcul des agrégats");
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            UUID sessionId = event.get("sessionId") != null
                    ? UUID.fromString(event.get("sessionId").toString())
                    : groupeId; // Fallback groupeId si sessionId manquant
            BigDecimal montant = extractBigDecimal(event.get("montant"));
            String mode = event.get("modePaiement") != null ? event.get("modePaiement").toString() : "CASH";

            statistiqueService.enregistrerCotisation(groupeId, sessionId, montant, mode);
        } catch (Exception ex) {
            log.error("Erreur traitement événement cotisation.payee : {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = "pot.verse", groupId = "ms-statistiques-group")
    public void onPotVerse(@Payload Map<String, Object> event) {
        log.info("Événement Kafka pot.verse reçu");
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            UUID sessionId = event.get("sessionId") != null
                    ? UUID.fromString(event.get("sessionId").toString())
                    : groupeId;
            BigDecimal montant = extractBigDecimal(event.get("montant"));

            statistiqueService.enregistrerDecaissement(groupeId, sessionId, montant);
        } catch (Exception ex) {
            log.error("Erreur traitement événement pot.verse : {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = "penalite.appliquee", groupId = "ms-statistiques-group")
    public void onPenaliteAppliquee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka penalite.appliquee reçu");
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            UUID sessionId = event.get("sessionId") != null
                    ? UUID.fromString(event.get("sessionId").toString())
                    : groupeId;
            BigDecimal montant = extractBigDecimal(event.get("montant"));

            statistiqueService.enregistrerPenalite(groupeId, sessionId, montant);
        } catch (Exception ex) {
            log.error("Erreur traitement événement penalite.appliquee : {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = "reunion.terminee", groupId = "ms-statistiques-group")
    public void onReunionTerminee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka reunion.terminee reçu");
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            UUID sessionId = event.get("sessionId") != null
                    ? UUID.fromString(event.get("sessionId").toString())
                    : groupeId;
            int presents = event.get("nbPresents") != null ? Integer.parseInt(event.get("nbPresents").toString()) : 1;
            int total = event.get("nbTotalMembres") != null ? Integer.parseInt(event.get("nbTotalMembres").toString()) : presents;

            statistiqueService.enregistrerReunion(groupeId, sessionId, presents, total);
        } catch (Exception ex) {
            log.error("Erreur traitement événement reunion.terminee : {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = "membre.inscrit", groupId = "ms-statistiques-group")
    public void onMembreInscrit(@Payload Map<String, Object> event) {
        log.info("Événement Kafka membre.inscrit reçu");
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            UUID sessionId = event.get("sessionId") != null
                    ? UUID.fromString(event.get("sessionId").toString())
                    : groupeId;

            statistiqueService.enregistrerMembre(groupeId, sessionId);
        } catch (Exception ex) {
            log.error("Erreur traitement événement membre.inscrit : {}", ex.getMessage(), ex);
        }
    }

    private BigDecimal extractBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(val.toString());
    }
}
