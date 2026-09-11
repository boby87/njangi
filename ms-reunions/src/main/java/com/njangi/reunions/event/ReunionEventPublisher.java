package com.njangi.reunions.event;

import com.njangi.reunions.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReunionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publierReunionTerminee(UUID reunionId, UUID groupeId, String titre) {
        Map<String, Object> event = Map.of(
                "reunionId", reunionId.toString(),
                "groupeId", groupeId.toString(),
                "titre", titre,
                "timestamp", Instant.now().toString()
        );

        kafkaTemplate.send(KafkaConfig.TOPIC_REUNION_TERMINEE, reunionId.toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Echec publication evenement reunion.terminee pour reunionId={}", reunionId, ex);
                    } else {
                        log.info("Evenement reunion.terminee publie pour reunionId={}, partition={}, offset={}",
                                reunionId,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
