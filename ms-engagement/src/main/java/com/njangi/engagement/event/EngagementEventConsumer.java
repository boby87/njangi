package com.njangi.engagement.event;

import com.njangi.engagement.config.KafkaConfig;
import com.njangi.engagement.notification.dto.EnvoyerNotificationRequest;
import com.njangi.engagement.notification.entity.Canal;
import com.njangi.engagement.notification.service.NotificationService;
import com.njangi.engagement.statistique.service.StatistiqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class EngagementEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EngagementEventConsumer.class);

    private final NotificationService notificationService;
    private final StatistiqueService statistiqueService;

    public EngagementEventConsumer(NotificationService notificationService,
                                   StatistiqueService statistiqueService) {
        this.notificationService = notificationService;
        this.statistiqueService = statistiqueService;
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_IDENTITE_EVENTS, groupId = "ms-engagement-group")
    public void onIdentiteEvent(@Payload DomainEvent event) {
        if (event == null || event.eventType() == null) {
            log.warn("Événement identite.events invalide ignoré (null)");
            return;
        }
        log.info("Événement identite.events reçu : type={} eventId={} groupeId={}",
                event.eventType(), event.eventId(), event.groupeId());
        Map<String, Object> payload = safePayload(event);

        try {
            switch (event.eventType()) {
                case "OTP_DEMANDE" -> handleOtpDemande(event, payload);
                case "UTILISATEUR_INSCRIT" -> handleUtilisateurInscrit(event, payload);
                case "MEMBRE_INSCRIT" -> handleMembreInscrit(event, payload);
                case "ROLE_ASSIGNE" -> handleRoleAssigne(event, payload);
                default -> log.debug("Type d'événement identite.events ignoré : {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de identite.events [{}] : {}", event.eventType(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_GOUVERNANCE_EVENTS, groupId = "ms-engagement-group")
    public void onGouvernanceEvent(@Payload DomainEvent event) {
        if (event == null || event.eventType() == null) {
            log.warn("Événement gouvernance.events invalide ignoré (null)");
            return;
        }
        log.info("Événement gouvernance.events reçu : type={} eventId={} groupeId={}",
                event.eventType(), event.eventId(), event.groupeId());
        Map<String, Object> payload = safePayload(event);

        try {
            switch (event.eventType()) {
                case "GROUPE_CREE" -> handleGroupeCree(event, payload);
                case "MEMBRE_AJOUTE" -> handleMembreInscrit(event, payload);
                case "SESSION_DEMARREE" -> handleSessionDemarree(event, payload);
                case "SESSION_CLOTUREE" -> handleSessionCloturee(event, payload);
                case "BUREAU_ELU" -> handleBureauElu(event, payload);
                case "REUNION_DEMARREE" -> handleReunionDemarree(event, payload);
                case "REUNION_TERMINEE" -> handleReunionTerminee(event, payload);
                default -> log.debug("Type d'événement gouvernance.events ignoré : {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de gouvernance.events [{}] : {}", event.eventType(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_FINANCES_EVENTS, groupId = "ms-engagement-group")
    public void onFinancesEvent(@Payload DomainEvent event) {
        if (event == null || event.eventType() == null) {
            log.warn("Événement finances.events invalide ignoré (null)");
            return;
        }
        log.info("Événement finances.events reçu : type={} eventId={} groupeId={}",
                event.eventType(), event.eventId(), event.groupeId());
        Map<String, Object> payload = safePayload(event);

        try {
            switch (event.eventType()) {
                case "COTISATION_PAYEE" -> handleCotisationPayee(event, payload);
                case "COTISATION_RETARD" -> handleCotisationRetard(event, payload);
                case "PAIEMENT_VALIDE" -> handlePaiementValide(event, payload);
                case "PAIEMENT_REJETE" -> handlePaiementRejete(event, payload);
                case "POT_ATTRIBUE" -> handlePotAttribue(event, payload);
                case "POT_VERSE" -> handlePotVerse(event, payload);
                case "SANCTION_INFLIGEE" -> handleSanctionInfligee(event, payload);
                case "SANCTION_PAYEE" -> handleSanctionPayee(event, payload);
                default -> log.debug("Type d'événement finances.events ignoré : {}", event.eventType());
            }
        } catch (Exception e) {
            log.error("Erreur lors du traitement de finances.events [{}] : {}", event.eventType(), e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Handlers identite.events
    // -------------------------------------------------------------------------

    private void handleOtpDemande(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "utilisateurId", "membreId", "destinataireId");
        if (destinataireId == null) {
            log.warn("OTP_DEMANDE sans destinataireId exploitable, ignoré");
            return;
        }
        String code = extractString(payload, "code", extractString(payload, "otp", "******"));
        String telephone = extractString(payload, "telephone", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                destinataireId,
                event.groupeId(),
                Canal.SMS,
                "OTP_DEMANDE",
                "Code de vérification Njangi",
                "Votre code OTP Njangi est : " + code + ". Il expire dans 5 minutes. Ne le partagez jamais.",
                null,
                "OTP",
                telephone
        ));
    }

    private void handleUtilisateurInscrit(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "utilisateurId", "membreId", "destinataireId");
        String nom = extractString(payload, "nomComplet", extractString(payload, "prenom", "Membre"));

        if (destinataireId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    destinataireId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "UTILISATEUR_INSCRIT",
                    "Bienvenue sur Njangi !",
                    "Bonjour " + nom + ", votre compte Njangi a été créé avec succès. Rejoignez ou créez votre tontine dès maintenant.",
                    destinataireId.toString(),
                    "UTILISATEUR",
                    extractString(payload, "email", extractString(payload, "telephone", null))
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerMembre(event.groupeId(), resolveSessionId(event, payload));
        }
    }

    private void handleMembreInscrit(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "membreId", "utilisateurId", "destinataireId");
        String nomGroupe = extractString(payload, "nomGroupe", "la tontine");

        if (destinataireId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    destinataireId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "MEMBRE_INSCRIT",
                    "Bienvenue dans " + nomGroupe,
                    "Votre adhésion a été validée avec succès. Vous avez accès à l'ensemble des fonctionnalités du groupe.",
                    event.groupeId() != null ? event.groupeId().toString() : null,
                    "GROUPE",
                    null
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerMembre(event.groupeId(), resolveSessionId(event, payload));
        }
    }

    private void handleRoleAssigne(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "membreId", "destinataireId", "utilisateurId");
        String role = extractString(payload, "role", "MEMBRE");
        String nomGroupe = extractString(payload, "nomGroupe", "votre groupe Njangi");

        if (destinataireId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    destinataireId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "ROLE_ASSIGNE",
                    "Nouveau rôle attribué : " + role,
                    "Vous avez été désigné(e) au rôle de " + role + " dans " + nomGroupe + ".",
                    event.groupeId() != null ? event.groupeId().toString() : null,
                    "ROLE",
                    null
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Handlers gouvernance.events
    // -------------------------------------------------------------------------

    private void handleGroupeCree(DomainEvent event, Map<String, Object> payload) {
        UUID createurId = resolveDestinataire(event, payload, "createurId", "destinataireId");
        if (createurId == null) {
            return;
        }
        String nom = extractString(payload, "nom", "Votre tontine");

        notificationService.envoyer(new EnvoyerNotificationRequest(
                createurId,
                event.groupeId(),
                Canal.IN_APP,
                "GROUPE_CREE",
                "Groupe Njangi créé !",
                "Votre groupe '" + nom + "' est opérationnel. Vous pouvez maintenant inviter des membres et paramétrer les sessions.",
                event.groupeId() != null ? event.groupeId().toString() : null,
                "GROUPE",
                null
        ));
    }

    private void handleSessionDemarree(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "destinataireId", "membreId");
        if (destinataireId == null) {
            return;
        }
        String libelle = extractString(payload, "libelle", "Nouvelle session");
        String sessionId = extractString(payload, "sessionId", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                destinataireId,
                event.groupeId(),
                Canal.IN_APP,
                "SESSION_DEMARREE",
                "Nouvelle session démarrée",
                "La session '" + libelle + "' vient de démarrer. Consultez votre calendrier de cotisations et l'ordre de passage du pot.",
                sessionId,
                "SESSION",
                null
        ));
    }

    private void handleSessionCloturee(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "destinataireId", "membreId");
        if (destinataireId == null) {
            return;
        }
        String libelle = extractString(payload, "libelle", "Session");
        String sessionId = extractString(payload, "sessionId", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                destinataireId,
                event.groupeId(),
                Canal.IN_APP,
                "SESSION_CLOTUREE",
                "Session clôturée",
                "La session '" + libelle + "' est désormais clôturée. Le bilan financier final est disponible.",
                sessionId,
                "SESSION",
                null
        ));
    }

    private void handleBureauElu(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "presidentId", "destinataireId", "membreId");
        if (destinataireId == null) {
            return;
        }
        String mandatId = extractString(payload, "mandatId", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                destinataireId,
                event.groupeId(),
                Canal.IN_APP,
                "BUREAU_ELU",
                "Nouveau bureau élu",
                "Le nouveau bureau exécutif a été officiellement élu et entre en fonction pour ce mandat.",
                mandatId,
                "MANDAT",
                null
        ));
    }

    private void handleReunionDemarree(DomainEvent event, Map<String, Object> payload) {
        UUID destinataireId = resolveDestinataire(event, payload, "destinataireId", "membreId");
        if (destinataireId == null) {
            return;
        }
        String lieu = extractString(payload, "lieu", "le siège");
        String reunionId = extractString(payload, "reunionId", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                destinataireId,
                event.groupeId(),
                Canal.IN_APP,
                "REUNION_DEMARREE",
                "Réunion démarrée",
                "La séance de tontine vient de démarrer (" + lieu + "). Pensez à enregistrer votre présence.",
                reunionId,
                "REUNION",
                null
        ));
    }

    private void handleReunionTerminee(DomainEvent event, Map<String, Object> payload) {
        if (event.groupeId() != null) {
            UUID sessionId = resolveSessionId(event, payload);
            int presents = extractInt(payload.get("nbPresents"), 1);
            int total = extractInt(payload.get("nbTotalMembres"), presents);
            statistiqueService.enregistrerReunion(event.groupeId(), sessionId, presents, total);
        }

        UUID destinataireId = resolveDestinataire(event, payload, "destinataireId", "membreId");
        if (destinataireId != null) {
            String reunionId = extractString(payload, "reunionId", null);
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    destinataireId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "REUNION_TERMINEE",
                    "Réunion clôturée — Compte-rendu disponible",
                    "La séance est terminée. Les présences et le procès-verbal sont disponibles dans votre espace groupe.",
                    reunionId,
                    "REUNION",
                    null
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Handlers finances.events
    // -------------------------------------------------------------------------

    private void handleCotisationPayee(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        BigDecimal montant = extractBigDecimal(payload.get("montant"));
        String mode = extractString(payload, "modePaiement", "CASH");

        if (membreId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "COTISATION_PAYEE",
                    "Cotisation validée",
                    "Votre versement de " + montant.stripTrailingZeros().toPlainString()
                            + " XAF a bien été validé et encaissé dans la caisse de la tontine.",
                    extractString(payload, "cotisationId", null),
                    "COTISATION",
                    null
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerCotisation(event.groupeId(), resolveSessionId(event, payload), montant, mode);
        }
    }

    private void handleCotisationRetard(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        if (membreId == null) {
            return;
        }
        BigDecimal montant = extractBigDecimal(payload.get("montant"));
        String telephone = extractString(payload, "telephone", null);

        notificationService.envoyer(new EnvoyerNotificationRequest(
                membreId,
                event.groupeId(),
                Canal.SMS,
                "COTISATION_RETARD",
                "Alerte retard de cotisation",
                "Rappel urgent: Votre cotisation de " + montant.stripTrailingZeros().toPlainString()
                        + " XAF est en retard. Merci de régulariser pour éviter les pénalités.",
                extractString(payload, "cotisationId", null),
                "COTISATION",
                telephone
        ));
    }

    private void handlePaiementValide(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        BigDecimal montant = extractBigDecimal(payload.get("montant"));
        String mode = extractString(payload, "modePaiement", "CASH");

        if (membreId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "PAIEMENT_VALIDE",
                    "Paiement confirmé",
                    "Votre paiement de " + montant.stripTrailingZeros().toPlainString()
                            + " XAF (" + mode + ") a été confirmé.",
                    extractString(payload, "paiementId", null),
                    "PAIEMENT",
                    null
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerCotisation(event.groupeId(), resolveSessionId(event, payload), montant, mode);
        }
    }

    private void handlePaiementRejete(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        if (membreId == null) {
            return;
        }
        String motif = extractString(payload, "motif", "Vérification impossible");

        notificationService.envoyer(new EnvoyerNotificationRequest(
                membreId,
                event.groupeId(),
                Canal.IN_APP,
                "PAIEMENT_REJETE",
                "Paiement rejeté",
                "Votre paiement n'a pas pu être validé. Motif : " + motif,
                extractString(payload, "paiementId", null),
                "PAIEMENT",
                null
        ));
    }

    private void handlePotAttribue(DomainEvent event, Map<String, Object> payload) {
        UUID beneficiaireId = resolveDestinataire(event, payload, "beneficiaireId", "membreId", "destinataireId");
        if (beneficiaireId == null) {
            return;
        }
        BigDecimal montant = extractBigDecimal(payload.get("montant"));

        notificationService.envoyer(new EnvoyerNotificationRequest(
                beneficiaireId,
                event.groupeId(),
                Canal.IN_APP,
                "POT_ATTRIBUE",
                "Attribution du pot de tontine",
                "Vous êtes le bénéficiaire désigné pour le pot d'un montant de "
                        + montant.stripTrailingZeros().toPlainString() + " XAF.",
                extractString(payload, "potId", null),
                "POT",
                null
        ));
    }

    private void handlePotVerse(DomainEvent event, Map<String, Object> payload) {
        UUID beneficiaireId = resolveDestinataire(event, payload, "beneficiaireId", "membreId", "destinataireId");
        BigDecimal montant = extractBigDecimal(payload.get("montant"));

        if (beneficiaireId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    beneficiaireId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "POT_VERSE",
                    "Félicitations ! Pot de tontine décaissé",
                    "C'est votre tour ! Le pot d'un montant de " + montant.stripTrailingZeros().toPlainString()
                            + " XAF vous a été officiellement attribué et versé.",
                    extractString(payload, "potId", null),
                    "POT",
                    null
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerDecaissement(event.groupeId(), resolveSessionId(event, payload), montant);
        }
    }

    private void handleSanctionInfligee(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        BigDecimal montant = extractBigDecimal(payload.get("montant"));
        String typeInfraction = extractString(payload, "typeInfraction",
                extractString(payload, "motif", "Infraction au règlement"));

        if (membreId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "SANCTION_INFLIGEE",
                    "Sanction disciplinaire appliquée",
                    "Une amende de " + montant.stripTrailingZeros().toPlainString()
                            + " XAF a été appliquée à votre encontre pour le motif : " + typeInfraction,
                    extractString(payload, "penaliteId", extractString(payload, "sanctionId", null)),
                    "PENALITE",
                    null
            ));
        }
    }

    private void handleSanctionPayee(DomainEvent event, Map<String, Object> payload) {
        UUID membreId = resolveDestinataire(event, payload, "membreId", "destinataireId");
        BigDecimal montant = extractBigDecimal(payload.get("montant"));

        if (membreId != null) {
            notificationService.envoyer(new EnvoyerNotificationRequest(
                    membreId,
                    event.groupeId(),
                    Canal.IN_APP,
                    "SANCTION_PAYEE",
                    "Sanction disciplinaire réglée",
                    "Votre règlement d'amende de " + montant.stripTrailingZeros().toPlainString()
                            + " XAF a bien été enregistré dans la caisse du groupe.",
                    extractString(payload, "penaliteId", extractString(payload, "sanctionId", null)),
                    "PENALITE",
                    null
            ));
        }

        if (event.groupeId() != null) {
            statistiqueService.enregistrerPenalite(event.groupeId(), resolveSessionId(event, payload), montant);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Map<String, Object> safePayload(DomainEvent event) {
        return event.payload() != null ? event.payload() : Map.of();
    }

    private UUID resolveDestinataire(DomainEvent event, Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object raw = payload.get(key);
            if (raw != null && !raw.toString().isBlank()) {
                return UUID.fromString(raw.toString());
            }
        }
        return event.acteurId();
    }

    private UUID resolveSessionId(DomainEvent event, Map<String, Object> payload) {
        Object raw = payload.get("sessionId");
        if (raw != null && !raw.toString().isBlank()) {
            return UUID.fromString(raw.toString());
        }
        return event.groupeId();
    }

    private String extractString(Map<String, Object> payload, String key, String defaultValue) {
        Object val = payload.get(key);
        return (val != null && !val.toString().isBlank()) ? val.toString() : defaultValue;
    }

    private BigDecimal extractBigDecimal(Object val) {
        if (val == null) {
            return BigDecimal.ZERO;
        }
        if (val instanceof BigDecimal bd) {
            return bd;
        }
        if (val instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        return new BigDecimal(val.toString());
    }

    private int extractInt(Object val, int defaultValue) {
        if (val == null) {
            return defaultValue;
        }
        if (val instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(val.toString());
    }
}
