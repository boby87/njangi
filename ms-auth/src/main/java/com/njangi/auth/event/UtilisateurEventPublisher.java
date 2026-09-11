package com.njangi.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UtilisateurEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOtpDemande(String telephone, String otp) {
        Map<String, Object> event = Map.of(
                "type", "OTP_DEMANDE",
                "telephone", telephone,
                "otp", otp,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send("notification.demandee", telephone, event);
        log.info("Evenement OTP_DEMANDE publie pour : {}", telephone);
    }

    public void publishConnexion(String utilisateurId) {
        Map<String, Object> event = Map.of(
                "type", "UTILISATEUR_CONNECTE",
                "utilisateurId", utilisateurId,
                "timestamp", Instant.now().toString()
        );
        kafkaTemplate.send("membre.inscrit", utilisateurId, event);
        log.info("Evenement UTILISATEUR_CONNECTE publie pour : {}", utilisateurId);
    }
}
