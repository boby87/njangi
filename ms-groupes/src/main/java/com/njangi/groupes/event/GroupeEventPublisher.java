package com.njangi.groupes.event;

import com.njangi.groupes.entity.Groupe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishGroupeCree(Groupe groupe) {
        Map<String, Object> event = Map.of(
                "type", "GROUPE_CREE",
                "groupeId", groupe.getId().toString(),
                "nom", groupe.getNom(),
                "createurMembreId", groupe.getCreateurMembreId().toString(),
                "typeSiege", groupe.getTypeSiege().name(),
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send("groupe.cree", groupe.getId().toString(), event);
        log.info("Evenement GROUPE_CREE publie pour : {}", groupe.getId());
    }
}
