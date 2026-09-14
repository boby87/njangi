package com.njangi.paiements.event;

import com.njangi.paiements.config.KafkaConfig;
import com.njangi.paiements.entity.ModePaiement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class PaiementEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaiementEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaiementEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierPaiementInitie(UUID paiementId, UUID cotisationId, UUID membreId,
                                      UUID groupeId, BigDecimal montant, ModePaiement modePaiement, String reference) {
        Map<String, Object> event = Map.of(
                "type", "paiement.initie",
                "paiementId", paiementId.toString(),
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "modePaiement", modePaiement.name(),
                "reference", reference != null ? reference : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_EVENTS, paiementId.toString(), event);
        log.info("Evenement paiement.initie publie : paiementId={}, mode={}, montant={}", paiementId, modePaiement, montant);
    }

    public void publierPaiementValide(UUID paiementId, UUID cotisationId, UUID membreId,
                                      UUID groupeId, BigDecimal montant, ModePaiement modePaiement, String reference) {
        Map<String, Object> event = Map.of(
                "type", "paiement.valide",
                "paiementId", paiementId.toString(),
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "modePaiement", modePaiement.name(),
                "reference", reference != null ? reference : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_VALIDE, paiementId.toString(), event);
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_EVENTS, paiementId.toString(), event);
        log.info("Evenement paiement.valide publie : paiementId={}, cotisationId={}, montant={}", paiementId, cotisationId, montant);
    }

    public void publierPaiementRejete(UUID paiementId, UUID cotisationId, UUID membreId, String motif) {
        Map<String, Object> event = Map.of(
                "type", "paiement.rejete",
                "paiementId", paiementId.toString(),
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "motif", motif != null ? motif : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_EVENTS, paiementId.toString(), event);
        log.info("Evenement paiement.rejete publie : paiementId={}, motif={}", paiementId, motif);
    }

    public void publierPaiementEchoue(UUID paiementId, UUID cotisationId, UUID membreId, String message) {
        Map<String, Object> event = Map.of(
                "type", "paiement.echoue",
                "paiementId", paiementId.toString(),
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "message", message != null ? message : "",
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_PAIEMENT_EVENTS, paiementId.toString(), event);
        log.info("Evenement paiement.echoue publie : paiementId={}, message={}", paiementId, message);
    }
}
