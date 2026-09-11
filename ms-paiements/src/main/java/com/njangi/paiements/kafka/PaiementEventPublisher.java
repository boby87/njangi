package com.njangi.paiements.kafka;

import com.njangi.paiements.entity.Paiement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaiementEventPublisher {

    private static final String TOPIC_PAIEMENT_VALIDE = "paiement.valide";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishPaiementValide(Paiement paiement) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PAIEMENT_VALIDE");
        event.put("paiementId", paiement.getId().toString());
        event.put("membreId", paiement.getMembreId().toString());
        event.put("groupeId", paiement.getGroupeId().toString());
        event.put("cotisationId", paiement.getCotisationId().toString());
        event.put("montant", paiement.getMontant());
        event.put("modePaiement", paiement.getModePaiement().name());
        event.put("validePar", paiement.getValidePar() != null ? paiement.getValidePar().toString() : null);
        event.put("valideLe", paiement.getValideLe() != null ? paiement.getValideLe().toString() : null);

        kafkaTemplate.send(TOPIC_PAIEMENT_VALIDE, paiement.getGroupeId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication de l'événement {} pour paiementId={}",
                                TOPIC_PAIEMENT_VALIDE, paiement.getId(), ex);
                    } else {
                        log.info("Événement {} publié — paiementId={} partition={} offset={}",
                                TOPIC_PAIEMENT_VALIDE, paiement.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
