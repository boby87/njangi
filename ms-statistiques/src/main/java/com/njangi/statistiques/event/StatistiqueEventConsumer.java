package com.njangi.statistiques.event;

import com.njangi.statistiques.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatistiqueEventConsumer {

    private final StatistiqueService statistiqueService;

    @KafkaListener(topics = "groupe.cree", groupId = "ms-statistiques-group")
    public void onGroupeCree(@Payload Map<String, Object> event) {
        log.info("Événement groupe.cree reçu : {}", event);
        // Initialisation des stats — l'appel à getOrCreate() dans le service créera la stat au premier événement réel
    }

    @KafkaListener(topics = "reunion.terminee", groupId = "ms-statistiques-group")
    public void onReunionTerminee(@Payload Map<String, Object> event) {
        log.info("Événement reunion.terminee reçu : {}", event);
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            statistiqueService.incrementerReunions(groupeId);
        } catch (Exception e) {
            log.error("Erreur traitement reunion.terminee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "cotisation.payee", groupId = "ms-statistiques-group")
    public void onCotisationPayee(@Payload Map<String, Object> event) {
        log.info("Événement cotisation.payee reçu : {}", event);
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            BigDecimal montant = new BigDecimal(event.get("montant").toString());
            statistiqueService.addCotisation(groupeId, montant);
        } catch (Exception e) {
            log.error("Erreur traitement cotisation.payee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "pot.verse", groupId = "ms-statistiques-group")
    public void onPotVerse(@Payload Map<String, Object> event) {
        log.info("Événement pot.verse reçu : {}", event);
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            statistiqueService.addPot(groupeId);
        } catch (Exception e) {
            log.error("Erreur traitement pot.verse : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "penalite.appliquee", groupId = "ms-statistiques-group")
    public void onPenaliteAppliquee(@Payload Map<String, Object> event) {
        log.info("Événement penalite.appliquee reçu : {}", event);
        try {
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            BigDecimal montant = new BigDecimal(event.get("montant").toString());
            statistiqueService.addPenalite(groupeId, montant);
        } catch (Exception e) {
            log.error("Erreur traitement penalite.appliquee : {}", e.getMessage(), e);
        }
    }
}
