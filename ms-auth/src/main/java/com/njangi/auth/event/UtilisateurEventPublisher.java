package com.njangi.auth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UtilisateurEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UtilisateurEventPublisher.class);
    private static final String TOPIC = "auth.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UtilisateurEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOtpDemande(String identifiant, String codeOtp) {
        AuthEvent event = AuthEvent.otpDemande(identifiant, codeOtp);
        try {
            kafkaTemplate.send(TOPIC, identifiant, event);
            log.info("Événement OTP_DEMANDE publié sur {} pour : {}", TOPIC, identifiant);
        } catch (Exception ex) {
            log.warn("Impossible de publier l'événement Kafka OTP (mode dégradé) : {}", ex.getMessage());
        }
    }

    public void publishUtilisateurInscrit(UUID utilisateurId, String identifiant, String nomComplet) {
        AuthEvent event = AuthEvent.utilisateurInscrit(utilisateurId, identifiant, nomComplet);
        try {
            kafkaTemplate.send(TOPIC, utilisateurId.toString(), event);
            log.info("Événement UTILISATEUR_INSCRIT publié sur {} pour : {}", TOPIC, identifiant);
        } catch (Exception ex) {
            log.warn("Impossible de publier l'événement Kafka Inscription : {}", ex.getMessage());
        }
    }

    public void publishUtilisateurConnecte(UUID utilisateurId, String identifiant) {
        AuthEvent event = AuthEvent.utilisateurConnecte(utilisateurId, identifiant);
        try {
            kafkaTemplate.send(TOPIC, utilisateurId.toString(), event);
            log.info("Événement UTILISATEUR_CONNECTE publié sur {} pour : {}", TOPIC, identifiant);
        } catch (Exception ex) {
            log.warn("Impossible de publier l'événement Kafka Connexion : {}", ex.getMessage());
        }
    }
}
