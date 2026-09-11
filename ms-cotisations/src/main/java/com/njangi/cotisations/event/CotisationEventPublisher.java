package com.njangi.cotisations.event;

import com.njangi.cotisations.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CotisationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publierCotisationPayee(UUID cotisationId, UUID membreId, UUID groupeId, BigDecimal montant) {
        Map<String, Object> event = Map.of(
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_PAYEE, cotisationId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec publication cotisation.payee pour cotisationId={}", cotisationId, ex);
                    } else {
                        log.info("Evenement cotisation.payee publie pour cotisationId={}", cotisationId);
                    }
                });
    }

    public void publierPotVerse(UUID potSessionId, UUID groupeId, UUID membreBeneficiaireId, BigDecimal montantTotal) {
        Map<String, Object> event = Map.of(
                "potSessionId", potSessionId.toString(),
                "groupeId", groupeId.toString(),
                "membreBeneficiaireId", membreBeneficiaireId.toString(),
                "montantTotal", montantTotal,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send(KafkaConfig.TOPIC_POT_VERSE, potSessionId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec publication pot.verse pour potSessionId={}", potSessionId, ex);
                    } else {
                        log.info("Evenement pot.verse publie pour potSessionId={}", potSessionId);
                    }
                });
    }
}
