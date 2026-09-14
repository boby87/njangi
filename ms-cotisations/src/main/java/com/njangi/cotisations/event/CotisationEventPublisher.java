package com.njangi.cotisations.event;

import com.njangi.cotisations.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
public class CotisationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CotisationEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CotisationEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publierCotisationGeneree(UUID cotisationId, UUID membreId, UUID groupeId, BigDecimal montant) {
        Map<String, Object> event = Map.of(
                "type", "cotisation.generee",
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_EVENTS, cotisationId.toString(), event);
        log.info("Evenement cotisation.generee publie : cotisationId={}, membreId={}, montant={}", cotisationId, membreId, montant);
    }

    public void publierCotisationPayee(UUID cotisationId, UUID membreId, UUID groupeId, BigDecimal montant) {
        Map<String, Object> event = Map.of(
                "type", "cotisation.payee",
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_PAYEE, cotisationId.toString(), event);
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_EVENTS, cotisationId.toString(), event);
        log.info("Evenement cotisation.payee publie : cotisationId={}, membreId={}, montant={}", cotisationId, membreId, montant);
    }

    public void publierPotAttribue(UUID potSessionId, UUID groupeId, UUID membreBeneficiaireId, BigDecimal montantTotal) {
        Map<String, Object> event = Map.of(
                "type", "pot.attribue",
                "potSessionId", potSessionId.toString(),
                "groupeId", groupeId.toString(),
                "membreBeneficiaireId", membreBeneficiaireId.toString(),
                "montantTotal", montantTotal,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_EVENTS, potSessionId.toString(), event);
        log.info("Evenement pot.attribue publie : potSessionId={}, beneficiaire={}", potSessionId, membreBeneficiaireId);
    }

    public void publierPotVerse(UUID potSessionId, UUID groupeId, UUID membreBeneficiaireId, BigDecimal montantTotal) {
        Map<String, Object> event = Map.of(
                "type", "pot.verse",
                "potSessionId", potSessionId.toString(),
                "groupeId", groupeId.toString(),
                "membreBeneficiaireId", membreBeneficiaireId.toString(),
                "montantTotal", montantTotal,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_POT_VERSE, potSessionId.toString(), event);
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_EVENTS, potSessionId.toString(), event);
        log.info("Evenement pot.verse publie : potSessionId={}, beneficiaire={}, montant={}", potSessionId, membreBeneficiaireId, montantTotal);
    }
}
