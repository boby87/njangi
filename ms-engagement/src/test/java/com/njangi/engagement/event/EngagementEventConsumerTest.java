package com.njangi.engagement.event;

import com.njangi.engagement.notification.dto.EnvoyerNotificationRequest;
import com.njangi.engagement.notification.entity.Canal;
import com.njangi.engagement.notification.service.NotificationService;
import com.njangi.engagement.statistique.service.StatistiqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EngagementEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private StatistiqueService statistiqueService;

    private EngagementEventConsumer consumer;

    private UUID groupeId;
    private UUID sessionId;
    private UUID acteurId;

    @BeforeEach
    void setUp() {
        consumer = new EngagementEventConsumer(notificationService, statistiqueService);
        groupeId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        acteurId = UUID.randomUUID();
    }

    // -------------------------------------------------------------------------
    // identite.events
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("identite.events [OTP_DEMANDE] -> envoi SMS OTP")
    void testIdentiteOtpDemande() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "OTP_DEMANDE",
                "ms-identite",
                null,
                acteurId,
                Instant.now(),
                Map.of("code", "482910", "telephone", "+237690112233")
        );

        consumer.onIdentiteEvent(event);

        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        EnvoyerNotificationRequest req = captor.getValue();
        assertThat(req.destinataireId()).isEqualTo(acteurId);
        assertThat(req.canal()).isEqualTo(Canal.SMS);
        assertThat(req.type()).isEqualTo("OTP_DEMANDE");
        assertThat(req.contenu()).contains("482910");
        assertThat(req.destinataireContact()).isEqualTo("+237690112233");
        verifyNoInteractions(statistiqueService);
    }

    @Test
    @DisplayName("identite.events [UTILISATEUR_INSCRIT] -> notification bienvenue + stats si groupeId présent")
    void testIdentiteUtilisateurInscrit() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "UTILISATEUR_INSCRIT",
                "ms-identite",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("nomComplet", "Jean Kamga", "sessionId", sessionId.toString())
        );

        consumer.onIdentiteEvent(event);

        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("UTILISATEUR_INSCRIT");
        assertThat(captor.getValue().canal()).isEqualTo(Canal.IN_APP);
        verify(statistiqueService).enregistrerMembre(groupeId, sessionId);
    }

    @Test
    @DisplayName("identite.events [MEMBRE_INSCRIT] -> notification bienvenue groupe + incrément nbMembres")
    void testIdentiteMembreInscrit() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "MEMBRE_INSCRIT",
                "ms-identite",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("nomGroupe", "Njangi Bamiléké", "sessionId", sessionId.toString())
        );

        consumer.onIdentiteEvent(event);

        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("MEMBRE_INSCRIT");
        assertThat(captor.getValue().titre()).contains("Njangi Bamiléké");
        verify(statistiqueService).enregistrerMembre(groupeId, sessionId);
    }

    @Test
    @DisplayName("identite.events [ROLE_ASSIGNE] -> notification attribution de rôle")
    void testIdentiteRoleAssigne() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "ROLE_ASSIGNE",
                "ms-identite",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("role", "TRESORIER", "nomGroupe", "Tontine Douala")
        );

        consumer.onIdentiteEvent(event);

        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("ROLE_ASSIGNE");
        assertThat(captor.getValue().titre()).contains("TRESORIER");
    }

    // -------------------------------------------------------------------------
    // gouvernance.events
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("gouvernance.events [GROUPE_CREE, SESSION_DEMARREE, BUREAU_ELU, REUNION_DEMARREE] -> notifications")
    void testGouvernanceNotifications() {
        consumer.onGouvernanceEvent(new DomainEvent(
                UUID.randomUUID(), "GROUPE_CREE", "ms-gouvernance", groupeId, acteurId, Instant.now(),
                Map.of("nom", "Tontine Solidarité")
        ));
        consumer.onGouvernanceEvent(new DomainEvent(
                UUID.randomUUID(), "SESSION_DEMARREE", "ms-gouvernance", groupeId, acteurId, Instant.now(),
                Map.of("libelle", "Session 2026-2027", "sessionId", sessionId.toString())
        ));
        consumer.onGouvernanceEvent(new DomainEvent(
                UUID.randomUUID(), "BUREAU_ELU", "ms-gouvernance", groupeId, acteurId, Instant.now(),
                Map.of("mandatId", UUID.randomUUID().toString())
        ));
        consumer.onGouvernanceEvent(new DomainEvent(
                UUID.randomUUID(), "REUNION_DEMARREE", "ms-gouvernance", groupeId, acteurId, Instant.now(),
                Map.of("lieu", "Siège Akwa", "reunionId", UUID.randomUUID().toString())
        ));

        verify(notificationService, times(4)).envoyer(any(EnvoyerNotificationRequest.class));
        verifyNoInteractions(statistiqueService);
    }

    @Test
    @DisplayName("gouvernance.events [REUNION_TERMINEE] -> agrégation assiduité + notification PV")
    void testGouvernanceReunionTerminee() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "REUNION_TERMINEE",
                "ms-gouvernance",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of(
                        "sessionId", sessionId.toString(),
                        "nbPresents", 18,
                        "nbTotalMembres", 20,
                        "reunionId", UUID.randomUUID().toString()
                )
        );

        consumer.onGouvernanceEvent(event);

        verify(statistiqueService).enregistrerReunion(groupeId, sessionId, 18, 20);
        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("REUNION_TERMINEE");
    }

    // -------------------------------------------------------------------------
    // finances.events
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("finances.events [COTISATION_PAYEE] -> notification + agrégation financière Cash/MoMo")
    void testFinancesCotisationPayee() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "COTISATION_PAYEE",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of(
                        "sessionId", sessionId.toString(),
                        "montant", 50000,
                        "modePaiement", "ORANGE_MONEY",
                        "cotisationId", UUID.randomUUID().toString()
                )
        );

        consumer.onFinancesEvent(event);

        verify(statistiqueService).enregistrerCotisation(
                eq(groupeId), eq(sessionId), eq(BigDecimal.valueOf(50000.0)), eq("ORANGE_MONEY"));
        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("COTISATION_PAYEE");
        assertThat(captor.getValue().canal()).isEqualTo(Canal.IN_APP);
    }

    @Test
    @DisplayName("finances.events [COTISATION_RETARD] -> alerte SMS sans impacter encaissements")
    void testFinancesCotisationRetard() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "COTISATION_RETARD",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("montant", 25000, "telephone", "+237677889900")
        );

        consumer.onFinancesEvent(event);

        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().canal()).isEqualTo(Canal.SMS);
        assertThat(captor.getValue().type()).isEqualTo("COTISATION_RETARD");
        assertThat(captor.getValue().destinataireContact()).isEqualTo("+237677889900");
        verifyNoInteractions(statistiqueService);
    }

    @Test
    @DisplayName("finances.events [PAIEMENT_VALIDE] -> notification + agrégation Cash")
    void testFinancesPaiementValide() {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "PAIEMENT_VALIDE",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("sessionId", sessionId.toString(), "montant", 10000, "modePaiement", "CASH")
        );

        consumer.onFinancesEvent(event);

        verify(statistiqueService).enregistrerCotisation(
                eq(groupeId), eq(sessionId), eq(BigDecimal.valueOf(10000.0)), eq("CASH"));
        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("PAIEMENT_VALIDE");
    }

    @Test
    @DisplayName("finances.events [POT_VERSE] -> notification bénéficiaire + agrégation décaissement")
    void testFinancesPotVerse() {
        UUID beneficiaireId = UUID.randomUUID();
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "POT_VERSE",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of(
                        "sessionId", sessionId.toString(),
                        "beneficiaireId", beneficiaireId.toString(),
                        "montant", 500000
                )
        );

        consumer.onFinancesEvent(event);

        verify(statistiqueService).enregistrerDecaissement(
                eq(groupeId), eq(sessionId), eq(BigDecimal.valueOf(500000.0)));
        ArgumentCaptor<EnvoyerNotificationRequest> captor = ArgumentCaptor.forClass(EnvoyerNotificationRequest.class);
        verify(notificationService).envoyer(captor.capture());
        assertThat(captor.getValue().destinataireId()).isEqualTo(beneficiaireId);
        assertThat(captor.getValue().type()).isEqualTo("POT_VERSE");
    }

    @Test
    @DisplayName("finances.events [SANCTION_INFLIGEE & SANCTION_PAYEE] -> notifications + agrégation pénalités")
    void testFinancesSanctions() {
        consumer.onFinancesEvent(new DomainEvent(
                UUID.randomUUID(),
                "SANCTION_INFLIGEE",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("montant", 2000, "typeInfraction", "Retard en réunion")
        ));

        consumer.onFinancesEvent(new DomainEvent(
                UUID.randomUUID(),
                "SANCTION_PAYEE",
                "ms-finances",
                groupeId,
                acteurId,
                Instant.now(),
                Map.of("sessionId", sessionId.toString(), "montant", 2000)
        ));

        verify(notificationService, times(2)).envoyer(any(EnvoyerNotificationRequest.class));
        verify(statistiqueService).enregistrerPenalite(
                eq(groupeId), eq(sessionId), eq(BigDecimal.valueOf(2000.0)));
    }

    @Test
    @DisplayName("Événement inconnu ou null est ignoré sans exception")
    void testUnknownOrNullEventIgnored() {
        consumer.onIdentiteEvent(null);
        consumer.onGouvernanceEvent(new DomainEvent(
                UUID.randomUUID(), "EVENEMENT_INCONNU", "ms-gouvernance", groupeId, acteurId, Instant.now(), Map.of()
        ));
        consumer.onFinancesEvent(new DomainEvent(
                UUID.randomUUID(), null, "ms-finances", groupeId, acteurId, Instant.now(), Map.of()
        ));

        verifyNoInteractions(notificationService);
        verifyNoInteractions(statistiqueService);
    }
}
