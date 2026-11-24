package com.njangi.identite.infrastructure.adapter.out.kafka;

import com.njangi.identite.domain.event.DomainEvent;
import com.njangi.identite.domain.event.MembreInscritEvent;
import com.njangi.identite.domain.event.RoleAssigneeEvent;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import com.njangi.identite.infrastructure.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaIdentiteEventPublisherAdapter implements IdentiteEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaIdentiteEventPublisherAdapter.class);
    private static final String TOPIC = KafkaConfig.IDENTITE_EVENTS_TOPIC;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaIdentiteEventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishOtpDemande(String identifiant, String codeOtp) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("identifiant", identifiant);
        payload.put("codeOtp", codeOtp);

        DomainEvent event = DomainEvent.of("OTP_DEMANDE", null, null, payload);
        send(identifiant, event);
    }

    @Override
    public void publishUtilisateurInscrit(UUID utilisateurId, String identifiant, String nomComplet) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("utilisateurId", utilisateurId.toString());
        payload.put("identifiant", identifiant);
        payload.put("nomComplet", nomComplet);

        DomainEvent event = DomainEvent.of("UTILISATEUR_INSCRIT", null, utilisateurId, payload);
        send(utilisateurId.toString(), event);
    }

    @Override
    public void publishUtilisateurConnecte(UUID utilisateurId, String identifiant) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("utilisateurId", utilisateurId.toString());
        payload.put("identifiant", identifiant);

        DomainEvent event = DomainEvent.of("UTILISATEUR_CONNECTE", null, utilisateurId, payload);
        send(utilisateurId.toString(), event);
    }

    @Override
    public void publishMembreInscrit(MembreInscritEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("membreId", event.membreId().toString());
        payload.put("authUtilisateurId", event.authUtilisateurId().toString());
        payload.put("nom", event.nom());
        payload.put("prenom", event.prenom());
        payload.put("email", event.email());
        payload.put("telephone", event.telephone());

        DomainEvent domainEvent = DomainEvent.of("MEMBRE_INSCRIT", null, event.authUtilisateurId(), payload);
        send(event.membreId().toString(), domainEvent);
    }

    @Override
    public void publishRoleAssigne(RoleAssigneeEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("adhesionId", event.adhesionId().toString());
        payload.put("utilisateurId", event.utilisateurId().toString());
        payload.put("groupeId", event.groupeId().toString());
        payload.put("role", event.nouveauRole().name());

        DomainEvent domainEvent = DomainEvent.of("ROLE_ASSIGNE", event.groupeId(), event.utilisateurId(), payload);
        send(event.utilisateurId().toString(), domainEvent);
    }

    private void send(String key, DomainEvent event) {
        try {
            kafkaTemplate.send(TOPIC, key, event);
            log.info("Événement {} publié sur {} (key={})", event.eventType(), TOPIC, key);
        } catch (Exception ex) {
            log.warn("Impossible de publier l'événement Kafka {} (mode dégradé) : {}", event.eventType(), ex.getMessage());
        }
    }
}
