package com.njangi.notifications.event;

import com.njangi.notifications.dto.EnvoyerNotificationRequest;
import com.njangi.notifications.entity.Canal;
import com.njangi.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;

    public NotificationEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "notification.demandee", groupId = "ms-notifications-group")
    public void onNotificationDemandee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka notification.demandee reçu : {}", event);
        try {
            UUID destinataireId = UUID.fromString(event.get("destinataireId").toString());
            UUID groupeId = event.get("groupeId") != null ? UUID.fromString(event.get("groupeId").toString()) : null;
            Canal canal = Canal.valueOf(event.getOrDefault("canal", "IN_APP").toString());
            String type = event.getOrDefault("type", "GENERIC").toString();
            String titre = event.getOrDefault("titre", "Notification Njangi").toString();
            String contenu = event.getOrDefault("contenu", "").toString();
            String referenceObjet = event.get("referenceObjet") != null ? event.get("referenceObjet").toString() : null;
            String typeObjet = event.get("typeObjet") != null ? event.get("typeObjet").toString() : null;
            String contact = event.get("destinataireContact") != null ? event.get("destinataireContact").toString() : null;

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    destinataireId, groupeId, canal, type, titre, contenu, referenceObjet, typeObjet, contact
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de notification.demandee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "groupe.cree", groupId = "ms-notifications-group")
    public void onGroupeCree(@Payload Map<String, Object> event) {
        log.info("Événement Kafka groupe.cree reçu : {}", event);
        try {
            UUID createurId = UUID.fromString(event.get("createurId").toString());
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            String nom = event.getOrDefault("nom", "Votre tontine").toString();

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    createurId,
                    groupeId,
                    Canal.IN_APP,
                    "GROUPE_CREE",
                    "Groupe Njangi créé !",
                    "Votre groupe '" + nom + "' est opérationnel. Vous pouvez maintenant inviter des membres et paramétrer les sessions.",
                    groupeId.toString(),
                    "GROUPE",
                    null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de groupe.cree : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "membre.inscrit", groupId = "ms-notifications-group")
    public void onMembreInscrit(@Payload Map<String, Object> event) {
        log.info("Événement Kafka membre.inscrit reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.get("membreId").toString());
            UUID groupeId = UUID.fromString(event.get("groupeId").toString());
            String nomGroupe = event.getOrDefault("nomGroupe", "la tontine").toString();

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    groupeId,
                    Canal.IN_APP,
                    "MEMBRE_INSCRIT",
                    "Bienvenue dans " + nomGroupe,
                    "Votre adhésion a été validée avec succès. Vous avez accès à l'ensemble des fonctionnalités du groupe.",
                    groupeId.toString(),
                    "GROUPE",
                    null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de membre.inscrit : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "cotisation.payee", groupId = "ms-notifications-group")
    public void onCotisationPayee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka cotisation.payee reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.get("membreId").toString());
            UUID groupeId = event.get("groupeId") != null ? UUID.fromString(event.get("groupeId").toString()) : null;
            Object montant = event.get("montant");

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    groupeId,
                    Canal.IN_APP,
                    "COTISATION_PAYEE",
                    "Cotisation validée",
                    "Votre versement de " + montant + " XAF a bien été validé et encaissé dans la caisse de la tontine.",
                    event.get("cotisationId") != null ? event.get("cotisationId").toString() : null,
                    "COTISATION",
                    null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de cotisation.payee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "cotisation.retard", groupId = "ms-notifications-group")
    public void onCotisationRetard(@Payload Map<String, Object> event) {
        log.info("Événement Kafka cotisation.retard reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.get("membreId").toString());
            UUID groupeId = event.get("groupeId") != null ? UUID.fromString(event.get("groupeId").toString()) : null;
            Object montant = event.get("montant");

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    groupeId,
                    Canal.SMS,
                    "COTISATION_RETARD",
                    "Alerte retard de cotisation",
                    "Rappel urgent: Votre cotisation de " + montant + " XAF est en retard. Merci de régulariser pour éviter les pénalités.",
                    event.get("cotisationId") != null ? event.get("cotisationId").toString() : null,
                    "COTISATION",
                    event.get("telephone") != null ? event.get("telephone").toString() : null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de cotisation.retard : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "penalite.appliquee", groupId = "ms-notifications-group")
    public void onPenaliteAppliquee(@Payload Map<String, Object> event) {
        log.info("Événement Kafka penalite.appliquee reçu : {}", event);
        try {
            UUID membreId = UUID.fromString(event.get("membreId").toString());
            UUID groupeId = event.get("groupeId") != null ? UUID.fromString(event.get("groupeId").toString()) : null;
            String typeInfraction = event.getOrDefault("typeInfraction", "Infraction au règlement").toString();
            Object montant = event.get("montant");

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    groupeId,
                    Canal.IN_APP,
                    "PENALITE_APPLIQUEE",
                    "Sanction disciplinaire appliquée",
                    "Une amende de " + montant + " XAF a été appliquée à votre encontre pour le motif : " + typeInfraction,
                    event.get("penaliteId") != null ? event.get("penaliteId").toString() : null,
                    "PENALITE",
                    null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de penalite.appliquee : {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "pot.verse", groupId = "ms-notifications-group")
    public void onPotVerse(@Payload Map<String, Object> event) {
        log.info("Événement Kafka pot.verse reçu : {}", event);
        try {
            UUID beneficiaireId = UUID.fromString(event.get("beneficiaireId").toString());
            UUID groupeId = event.get("groupeId") != null ? UUID.fromString(event.get("groupeId").toString()) : null;
            Object montant = event.get("montant");

            notificationService.envoyer(new EnvoyerNotificationRequest(
                    beneficiaireId,
                    groupeId,
                    Canal.IN_APP,
                    "POT_VERSE",
                    "Félicitations ! Pot de tontine décaissé",
                    "C'est votre tour ! Le pot d'un montant de " + montant + " XAF vous a été officiellement attribué et versé.",
                    event.get("potId") != null ? event.get("potId").toString() : null,
                    "POT",
                    null
            ));
        } catch (Exception e) {
            log.error("Erreur lors du traitement de pot.verse : {}", e.getMessage(), e);
        }
    }
}
