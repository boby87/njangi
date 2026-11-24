package com.njangi.engagement.event;

import com.njangi.engagement.config.KafkaConfig;
import com.njangi.engagement.notification.entity.Notification;
import com.njangi.engagement.statistique.dto.BilanFinancierDto;
import com.njangi.engagement.statistique.entity.StatistiqueGroupe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EngagementEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EngagementEventPublisher.class);

    public static final String EVENT_NOTIFICATION_ENVOYEE = "NOTIFICATION_ENVOYEE";
    public static final String EVENT_BILAN_GENERE = "BILAN_GENERE";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EngagementEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishNotificationEnvoyee(Notification notification) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("notificationId", notification.getId() != null ? notification.getId().toString() : null);
        payload.put("destinataireId", notification.getDestinataireId().toString());
        payload.put("groupeId", notification.getGroupeId() != null ? notification.getGroupeId().toString() : null);
        payload.put("canal", notification.getCanal().name());
        payload.put("type", notification.getType());
        payload.put("titre", notification.getTitre());
        payload.put("contenu", notification.getContenu());
        payload.put("statut", notification.getStatut().name());

        DomainEvent event = DomainEvent.of(
                EVENT_NOTIFICATION_ENVOYEE,
                notification.getGroupeId(),
                notification.getDestinataireId(),
                payload
        );

        String key = notification.getDestinataireId().toString();
        sendEvent(key, event);
    }

    public void publishBilanGenere(StatistiqueGroupe stat) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("statistiqueId", stat.getId() != null ? stat.getId().toString() : null);
        payload.put("groupeId", stat.getGroupeId().toString());
        payload.put("sessionId", stat.getSessionId().toString());
        payload.put("totalCollecte", stat.getTotalCollecte());
        payload.put("totalDecaisse", stat.getTotalDecaisse());
        payload.put("soldeCaisse", stat.getSoldeCaisse());
        payload.put("totalPenalites", stat.getTotalPenalites());
        payload.put("totalCash", stat.getTotalCash());
        payload.put("totalMomo", stat.getTotalMomo());
        payload.put("nbMembres", stat.getNbMembres());
        payload.put("nbReunions", stat.getNbReunions());
        payload.put("tauxParticipation", stat.getTauxParticipation());
        payload.put("tauxPresence", stat.getTauxPresence());
        payload.put("calculeLe", stat.getCalculeLe() != null ? stat.getCalculeLe().toString() : null);

        DomainEvent event = DomainEvent.of(
                EVENT_BILAN_GENERE,
                stat.getGroupeId(),
                null,
                payload
        );

        sendEvent(stat.getGroupeId().toString(), event);
    }

    public void publishBilanGenere(BilanFinancierDto bilan) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("groupeId", bilan.groupeId().toString());
        payload.put("sessionId", bilan.sessionId().toString());
        payload.put("soldeGlobalXAF", bilan.soldeGlobalXAF());
        payload.put("totalCotisationsXAF", bilan.totalCotisationsXAF());
        payload.put("totalDecaissementsXAF", bilan.totalDecaissementsXAF());
        payload.put("totalPenalitesXAF", bilan.totalPenalitesXAF());
        payload.put("repartitionTresorerie", bilan.repartitionTresorerie());
        payload.put("tauxRecouvrement", bilan.tauxRecouvrement());
        payload.put("tauxAssiduite", bilan.tauxAssiduite());
        payload.put("statutSanteFinanciere", bilan.statutSanteFinanciere());
        payload.put("nbMembres", bilan.nbMembres());
        payload.put("nbReunions", bilan.nbReunions());
        payload.put("genereLe", bilan.genereLe() != null ? bilan.genereLe().toString() : null);

        DomainEvent event = DomainEvent.of(
                EVENT_BILAN_GENERE,
                bilan.groupeId(),
                null,
                payload
        );

        sendEvent(bilan.groupeId().toString(), event);
    }

    private void sendEvent(String key, DomainEvent event) {
        kafkaTemplate.send(KafkaConfig.TOPIC_ENGAGEMENT_EVENTS, key, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Erreur lors de la publication de {} sur {} : {}",
                                event.eventType(), KafkaConfig.TOPIC_ENGAGEMENT_EVENTS, ex.getMessage());
                    } else {
                        log.info("Événement {} publié sur {} (eventId={})",
                                event.eventType(), KafkaConfig.TOPIC_ENGAGEMENT_EVENTS, event.eventId());
                    }
                });
    }
}
