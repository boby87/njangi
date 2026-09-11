package com.njangi.notifications.event;

import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.entity.TypeNotification;
import com.njangi.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "notification.demandee", groupId = "ms-notifications-group")
    public void onNotificationDemandee(@Payload Map<String, Object> event) {
        log.info("Événement notification.demandee reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.getOrDefault("membreId", "").toString());
            String destinataire = event.getOrDefault("destinataire", "").toString();
            String sujet = event.getOrDefault("sujet", "Notification Njangi").toString();
            String contenu = event.getOrDefault("contenu", "").toString();
            String typeStr = event.getOrDefault("type", "IN_APP").toString();
            TypeNotification type = TypeNotification.valueOf(typeStr);

            EnvoyerNotificationRequest request = new EnvoyerNotificationRequest(
                    membreId, type, sujet, contenu, destinataire,
                    event.getOrDefault("referenceObjet", "").toString(),
                    event.getOrDefault("typeObjet", "").toString());
            notificationService.envoyerNotification(request);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de notification.demandee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "cotisation.payee", groupId = "ms-notifications-group")
    public void onCotisationPayee(@Payload Map<String, Object> event) {
        log.info("Événement cotisation.payee reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.getOrDefault("membreId", "").toString());
            String montant = event.getOrDefault("montant", "").toString();
            EnvoyerNotificationRequest request = new EnvoyerNotificationRequest(
                    membreId, TypeNotification.IN_APP,
                    "Cotisation enregistrée",
                    "Votre cotisation de " + montant + " FCFA a été enregistrée avec succès.",
                    membreId.toString(),
                    event.getOrDefault("cotisationId", "").toString(),
                    "COTISATION");
            notificationService.envoyerNotification(request);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de cotisation.payee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "pot.verse", groupId = "ms-notifications-group")
    public void onPotVerse(@Payload Map<String, Object> event) {
        log.info("Événement pot.verse reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.getOrDefault("membreId", "").toString());
            String montant = event.getOrDefault("montant", "").toString();
            EnvoyerNotificationRequest request = new EnvoyerNotificationRequest(
                    membreId, TypeNotification.IN_APP,
                    "Pot versé",
                    "Le pot de " + montant + " FCFA vous a été versé. Félicitations !",
                    membreId.toString(),
                    event.getOrDefault("potId", "").toString(),
                    "POT");
            notificationService.envoyerNotification(request);
        } catch (Exception e) {
            log.error("Erreur lors du traitement de pot.verse : {}", e.getMessage(), e);
        }
    }
}
