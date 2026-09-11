package com.njangi.paiements.event;

import com.njangi.paiements.config.KafkaConfig;
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
public class PaiementEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publierPaiementValide(UUID paiementId, UUID cotisationId, UUID membreId, UUID groupeId, BigDecimal montant) {
        Map<String, Object> event = Map.of(
                "paiementId", paiementId.toString(),
                "cotisationId", cotisationId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "timestamp", Instant.now().toString()
        );

        kafkaTemplate.send(KafkaConfig.TOPIC_COTISATION_PAYEE, paiementId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec publication cotisation.payee pour paiementId={}", paiementId, ex);
                    } else {
                        log.info("Evenement cotisation.payee publie pour paiementId={}, partition={}, offset={}",
                                paiementId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
