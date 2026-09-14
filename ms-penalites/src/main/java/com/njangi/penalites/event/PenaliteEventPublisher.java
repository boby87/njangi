package com.njangi.penalites.event;

import com.njangi.penalites.config.KafkaConfig;
import com.njangi.penalites.entity.Penalite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class PenaliteEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PenaliteEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PenaliteEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierPenaliteInfligee(Penalite penalite) {
        Map<String, Object> event = Map.of(
                "type", "sanction.infligee",
                "penaliteId", penalite.getId().toString(),
                "membreId", penalite.getMembreId().toString(),
                "groupeId", penalite.getGroupeId().toString(),
                "sessionId", penalite.getSessionId().toString(),
                "reunionId", penalite.getReunionId() != null ? penalite.getReunionId().toString() : "",
                "typeInfraction", penalite.getTypeInfraction().name(),
                "montant", penalite.getMontant(),
                "statut", penalite.getStatut().name(),
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_SANCTION_EVENTS, penalite.getId().toString(), event);
        log.info("Evenement sanction.infligee publie : id={}, type={}, montant={}",
                penalite.getId(), penalite.getTypeInfraction(), penalite.getMontant());
    }

    public void publierPenalitePayee(Penalite penalite) {
        Map<String, Object> event = Map.of(
                "type", "sanction.payee",
                "penaliteId", penalite.getId().toString(),
                "membreId", penalite.getMembreId().toString(),
                "groupeId", penalite.getGroupeId().toString(),
                "montant", penalite.getMontant(),
                "datePaiement", penalite.getDatePaiement() != null ? penalite.getDatePaiement().toString() : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_SANCTION_EVENTS, penalite.getId().toString(), event);
        log.info("Evenement sanction.payee publie : id={}, montant={}", penalite.getId(), penalite.getMontant());
    }

    public void publierPenaliteAnnulee(Penalite penalite) {
        Map<String, Object> event = Map.of(
                "type", "sanction.annulee",
                "penaliteId", penalite.getId().toString(),
                "membreId", penalite.getMembreId().toString(),
                "groupeId", penalite.getGroupeId().toString(),
                "motif", penalite.getMotif() != null ? penalite.getMotif() : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_SANCTION_EVENTS, penalite.getId().toString(), event);
        log.info("Evenement sanction.annulee publie : id={}", penalite.getId());
    }
}
