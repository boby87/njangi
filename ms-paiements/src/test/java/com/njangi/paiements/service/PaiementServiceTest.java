package com.njangi.paiements.service;

import com.njangi.paiements.dto.*;
import com.njangi.paiements.entity.ModePaiement;
import com.njangi.paiements.entity.Paiement;
import com.njangi.paiements.entity.StatutPaiement;
import com.njangi.paiements.event.PaiementEventPublisher;
import com.njangi.paiements.exception.BusinessException;
import com.njangi.paiements.repository.PaiementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaiementServiceTest {

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private PaiementEventPublisher eventPublisher;

    @InjectMocks
    private PaiementService paiementService;

    private UUID cotisationId;
    private UUID membreId;
    private UUID groupeId;
    private UUID tresorierId;

    @BeforeEach
    void setUp() {
        cotisationId = UUID.randomUUID();
        membreId = UUID.randomUUID();
        groupeId = UUID.randomUUID();
        tresorierId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit initier un paiement Cash avec pièce jointe (reçu signé) obligatoire")
    void doitInitierPaiementCashAvecPieceJointe() {
        String cleIdempotence = "IDEM-CASH-001";
        InitierPaiementCashRequest request = new InitierPaiementCashRequest(
                cotisationId,
                membreId,
                groupeId,
                BigDecimal.valueOf(50000),
                cleIdempotence,
                "https://njangi.cm/uploads/recus/recu_signe_123.jpg",
                "Versement en espèces en séance"
        );

        when(paiementRepository.findByCleIdempotence(cleIdempotence)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaiementDto result = paiementService.initierPaiementCash(request);

        assertThat(result).isNotNull();
        assertThat(result.modePaiement()).isEqualTo(ModePaiement.CASH);
        assertThat(result.statut()).isEqualTo(StatutPaiement.EN_ATTENTE_VALIDATION);
        assertThat(result.pieceJointeUrl()).isEqualTo("https://njangi.cm/uploads/recus/recu_signe_123.jpg");
        assertThat(result.reference()).startsWith("CASH-");

        verify(paiementRepository).save(any(Paiement.class));
        verify(eventPublisher).publierPaiementInitie(any(), eq(cotisationId), eq(membreId),
                eq(groupeId), eq(BigDecimal.valueOf(50000)), eq(ModePaiement.CASH), any());
    }

    @Test
    @DisplayName("Doit bloquer et refuser un paiement Cash sans pièce jointe du reçu signé")
    void doitBloquerPaiementCashSansPieceJointe() {
        InitierPaiementCashRequest request = new InitierPaiementCashRequest(
                cotisationId,
                membreId,
                groupeId,
                BigDecimal.valueOf(50000),
                "IDEM-CASH-NO-PROOF",
                null, // Preuve absente
                "Paiement sans reçu"
        );

        when(paiementRepository.findByCleIdempotence("IDEM-CASH-NO-PROOF")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paiementService.initierPaiementCash(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("reçu physique signé");

        verify(paiementRepository, never()).save(any());
        verify(eventPublisher, never()).publierPaiementInitie(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Doit valider un paiement Cash par le trésorier et émettre l'événement Kafka")
    void doitValiderPaiementCashParTresorier() {
        UUID paiementId = UUID.randomUUID();
        Paiement paiement = new Paiement(
                paiementId, cotisationId, membreId, groupeId,
                BigDecimal.valueOf(50000), ModePaiement.CASH, "IDEM-002", "CASH-ABCDEF12",
                "https://njangi.cm/recu.pdf", null, "ESPECES", StatutPaiement.EN_ATTENTE_VALIDATION
        );

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ValiderPaiementCashRequest request = new ValiderPaiementCashRequest(tresorierId, true, "Reçu physique vérifié et contresigné");

        PaiementDto result = paiementService.validerPaiementCash(paiementId, request);

        assertThat(result.statut()).isEqualTo(StatutPaiement.VALIDE);
        assertThat(result.validePar()).isEqualTo(tresorierId);
        assertThat(result.dateValidation()).isNotNull();

        verify(eventPublisher).publierPaiementValide(eq(paiementId), eq(cotisationId), eq(membreId),
                eq(groupeId), eq(BigDecimal.valueOf(50000)), eq(ModePaiement.CASH), eq("CASH-ABCDEF12"));
    }

    @Test
    @DisplayName("Doit rejeter un paiement Cash non conforme par le trésorier")
    void doitRejeterPaiementCashParTresorier() {
        UUID paiementId = UUID.randomUUID();
        Paiement paiement = new Paiement(
                paiementId, cotisationId, membreId, groupeId,
                BigDecimal.valueOf(50000), ModePaiement.CASH, "IDEM-003", "CASH-XYZ12345",
                "https://njangi.cm/illisible.jpg", null, "ESPECES", StatutPaiement.EN_ATTENTE_VALIDATION
        );

        when(paiementRepository.findById(paiementId)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ValiderPaiementCashRequest request = new ValiderPaiementCashRequest(tresorierId, false, "Reçu illisible, montant non vérifiable");

        PaiementDto result = paiementService.validerPaiementCash(paiementId, request);

        assertThat(result.statut()).isEqualTo(StatutPaiement.REJETE);
        assertThat(result.validePar()).isEqualTo(tresorierId);

        verify(eventPublisher).publierPaiementRejete(eq(paiementId), eq(cotisationId), eq(membreId), eq("Reçu illisible, montant non vérifiable"));
    }

    @Test
    @DisplayName("Doit initier un paiement Mobile Money avec clé d'idempotence")
    void doitInitierPaiementMobileMoney() {
        String cleIdempotence = "IDEM-MOMO-12345";
        InitierPaiementMobileMoneyRequest request = new InitierPaiementMobileMoneyRequest(
                cotisationId,
                membreId,
                groupeId,
                BigDecimal.valueOf(25000),
                cleIdempotence,
                "+237670000000",
                ModePaiement.MTN_MOMO
        );

        when(paiementRepository.findByCleIdempotence(cleIdempotence)).thenReturn(Optional.empty());
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> {
            Paiement p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        PaiementDto result = paiementService.initierPaiementMobileMoney(request);

        assertThat(result).isNotNull();
        assertThat(result.modePaiement()).isEqualTo(ModePaiement.MTN_MOMO);
        assertThat(result.statut()).isEqualTo(StatutPaiement.EN_COURS);
        assertThat(result.reference()).startsWith("MOMO-");
        assertThat(result.numeroTelephone()).isEqualTo("+237670000000");

        verify(eventPublisher).publierPaiementInitie(any(), eq(cotisationId), eq(membreId),
                eq(groupeId), eq(BigDecimal.valueOf(25000)), eq(ModePaiement.MTN_MOMO), any());
    }

    @Test
    @DisplayName("Doit retourner le paiement existant sans recréation si la clé d'idempotence existe déjà")
    void doitRespecterIdempotenceMobileMoney() {
        String cleIdempotence = "IDEM-MOMO-EXISTANT";
        Paiement existant = new Paiement(
                UUID.randomUUID(), cotisationId, membreId, groupeId,
                BigDecimal.valueOf(25000), ModePaiement.MTN_MOMO, cleIdempotence, "MOMO-998877",
                null, "+237670000000", "MTN_MOMO", StatutPaiement.EN_COURS
        );

        when(paiementRepository.findByCleIdempotence(cleIdempotence)).thenReturn(Optional.of(existant));

        InitierPaiementMobileMoneyRequest request = new InitierPaiementMobileMoneyRequest(
                cotisationId, membreId, groupeId, BigDecimal.valueOf(25000), cleIdempotence, "+237670000000", ModePaiement.MTN_MOMO
        );

        PaiementDto result = paiementService.initierPaiementMobileMoney(request);

        assertThat(result.id()).isEqualTo(existant.getId());
        assertThat(result.reference()).isEqualTo("MOMO-998877");
        verify(paiementRepository, never()).save(any());
        verify(eventPublisher, never()).publierPaiementInitie(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Doit traiter un webhook opérateur Mobile Money en succès de manière idempotente")
    void doitTraiterWebhookSuccesIdempotent() {
        String ref = "MOMO-WEBHOOK-SUCCESS";
        Paiement paiement = new Paiement(
                UUID.randomUUID(), cotisationId, membreId, groupeId,
                BigDecimal.valueOf(25000), ModePaiement.MTN_MOMO, "IDEM-WH-1", ref,
                null, "+237670000000", "MTN_MOMO", StatutPaiement.EN_COURS
        );

        when(paiementRepository.findByReference(ref)).thenReturn(Optional.of(paiement));
        when(paiementRepository.save(any(Paiement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WebhookPaiementRequest webhookReq = new WebhookPaiementRequest(
                ref, "IDEM-WH-1", true, "EXT-MTN-9999", "Transaction validée"
        );

        PaiementDto result = paiementService.traiterWebhook("MTN_MOMO", webhookReq);

        assertThat(result.statut()).isEqualTo(StatutPaiement.VALIDE);
        assertThat(result.dateValidation()).isNotNull();
        verify(eventPublisher).publierPaiementValide(eq(paiement.getId()), eq(cotisationId), eq(membreId),
                eq(groupeId), eq(BigDecimal.valueOf(25000)), eq(ModePaiement.MTN_MOMO), eq(ref));

        // Deuxième appel webhook identique (idempotence)
        PaiementDto result2 = paiementService.traiterWebhook("MTN_MOMO", webhookReq);
        assertThat(result2.statut()).isEqualTo(StatutPaiement.VALIDE);
        verify(eventPublisher, times(1)).publierPaiementValide(any(), any(), any(), any(), any(), any(), any());
    }
}
