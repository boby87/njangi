package com.njangi.notifications.kafka;

import com.njangi.notifications.dto.NotificationDto;
import com.njangi.notifications.entity.Notification.Canal;
import com.njangi.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Consommateur d'événements Kafka pour ms-notifications.
 * Crée automatiquement des notifications IN_APP à chaque événement domaine significatif.
 * L'envoi sur les autres canaux (FCM, SMS, Email) est déclenché via "notification.demandee".
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "groupe.cree", groupId = "ms-notifications")
    public void onGroupeCree(Map<String, Object> event) {
        log.info("Événement groupe.cree reçu : groupeId={}", event.get("groupeId"));
        try {
            UUID membreId = UUID.fromString((String) event.get("createurId"));
            UUID groupeId = UUID.fromString((String) event.get("groupeId"));
            notificationService.envoyer(new NotificationDto(
                    null, membreId, groupeId, Canal.IN_APP,
                    "GROUPE_CREE",
                    "Groupe créé avec succès",
                    "Votre groupe Njangi a bien été créé. Invitez des membres pour commencer.",
                    null, false, null, null
            ));
        } catch (Exception ex) {
            log.error("Erreur traitement groupe.cree", ex);
        }
    }

    @KafkaListener(topics = "membre.inscrit", groupId = "ms-notifications")
    public void onMembreInscrit(Map<String, Object> event) {
        log.info("Événement membre.inscrit reçu");
        try {
            UUID membreId = UUID.fromString((String) event.get("membreId"));
            UUID groupeId = UUID.fromString((String) event.get("groupeId"));
            String nomGroupe = (String) event.getOrDefault("nomGroupe", "un groupe");
            notificationService.envoyer(new NotificationDto(
                    null, membreId, groupeId, Canal.IN_APP,
                    "MEMBRE_INSCRIT",
                    "Inscription confirmée",
                    "Vous avez été inscrit dans le groupe : " + nomGroupe,
                    null, false, null, null
            ));
        } catch (Exception ex) {
            log.error("Erreur traitement membre.inscrit", ex);
        }
    }

    @KafkaListener(topics = "cotisation.payee", groupId = "ms-notifications")
    public void onCotisationPayee(Map<String, Object> event) {
        log.info("Événement cotisation.payee reçu");
        try {
            UUID membreId = UUID.fromString((String) event.get("membreId"));
            UUID groupeId = UUID.fromString((String) event.get("groupeId"));
            Object montant = event.get("montant");
            notificationService.envoyer(new NotificationDto(
                    null, membreId, groupeId, Canal.IN_APP,
                    "COTISATION_PAYEE",
                    "Paiement confirmé",
                    "Votre cotisation de " + montant + " FCFA a été enregistrée.",
                    null, false, null, null
            ));
        } catch (Exception ex) {
            log.error("Erreur traitement cotisation.payee", ex);
        }
    }

    @KafkaListener(topics = "penalite.appliquee", groupId = "ms-notifications")
    public void onPenaliteAppliquee(Map<String, Object> event) {
        log.info("Événement penalite.appliquee reçu");
        try {
            UUID membreId  = UUID.fromString((String) event.get("membreId"));
            UUID groupeId  = UUID.fromString((String) event.get("groupeId"));
            String type    = (String) event.get("typeInfraction");
            Object montant = event.get("montant");
            notificationService.envoyer(new NotificationDto(
                    null, membreId, groupeId, Canal.IN_APP,
                    "PENALITE_APPLIQUEE",
                    "Pénalité appliquée",
                    "Une pénalité de " + montant + " FCFA a été appliquée pour : " + type,
                    null, false, null, null
            ));
        } catch (Exception ex) {
            log.error("Erreur traitement penalite.appliquee", ex);
        }
    }

    @KafkaListener(topics = "reunion.terminee", groupId = "ms-notifications")
    public void onReunionTerminee(Map<String, Object> event) {
        log.info("Événement reunion.terminee reçu");
        try {
            UUID groupeId = UUID.fromString((String) event.get("groupeId"));
            // Notification de masse — le groupe entier est notifié via le groupeId
            // Dans une implémentation complète, récupérer tous les membres via ms-membres
            log.info("Réunion terminée pour groupeId={} — TODO: notifier tous les membres", groupeId);
        } catch (Exception ex) {
            log.error("Erreur traitement reunion.terminee", ex);
        }
    }
}
