package com.njangi.penalites.event;

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
public class PenaliteEventPublisher {

    private static final String TOPIC = "penalite.appliquee";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publierPenaliteAppliquee(UUID penaliteId, UUID membreId, UUID groupeId,
                                          BigDecimal montant, String motif) {
        Map<String, Object> event = Map.of(
                "penaliteId", penaliteId.toString(),
                "membreId", membreId.toString(),
                "groupeId", groupeId.toString(),
                "montant", montant,
                "motif", motif,
                "timestamp", Instant.now().toString()
        );
        log.info("Publication événement {} : penaliteId={}", TOPIC, penaliteId);
        kafkaTemplate.send(TOPIC, penaliteId.toString(), event);
    }
}
