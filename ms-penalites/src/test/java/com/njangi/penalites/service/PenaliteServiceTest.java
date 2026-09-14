package com.njangi.penalites.service;

import com.njangi.penalites.dto.InfligerPenaliteRequest;
import com.njangi.penalites.dto.PenaliteDto;
import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.StatutPenalite;
import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import com.njangi.penalites.event.PenaliteEventPublisher;
import com.njangi.penalites.exception.BusinessException;
import com.njangi.penalites.repository.PenaliteRepository;
import com.njangi.penalites.repository.TarificationPenaliteRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PenaliteServiceTest {

    @Mock
    private PenaliteRepository penaliteRepository;

    @Mock
    private TarificationPenaliteRepository tarificationRepository;

    @Mock
    private PenaliteEventPublisher eventPublisher;

    @InjectMocks
    private PenaliteService penaliteService;

    private UUID membreId;
    private UUID groupeId;
    private UUID sessionId;
    private UUID reunionId;

    @BeforeEach
    void setUp() {
        membreId = UUID.randomUUID();
        groupeId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        reunionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit infliger une pénalité avec résolution automatique du montant via le barème du groupe")
    void doitInfligerPenaliteAvecTarifAutomatique() {
        TarificationPenalite tarif = new TarificationPenalite(
                UUID.randomUUID(), groupeId, TypeInfraction.RETARD_REUNION, BigDecimal.valueOf(1000), true
        );

        when(tarificationRepository.findByGroupeIdAndTypeInfractionAndActifTrue(groupeId, TypeInfraction.RETARD_REUNION))
                .thenReturn(Optional.of(tarif));

        when(penaliteRepository.save(any(Penalite.class)))
                .thenAnswer(invocation -> {
                    Penalite p = invocation.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });

        InfligerPenaliteRequest request = new InfligerPenaliteRequest(
                membreId, groupeId, sessionId, reunionId, TypeInfraction.RETARD_REUNION, null, "Arrivée 20 minutes en retard"
        );

        PenaliteDto result = penaliteService.infligerPenalite(request);

        assertThat(result).isNotNull();
        assertThat(result.montant()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.statut()).isEqualTo(StatutPenalite.EN_ATTENTE);
        assertThat(result.typeInfraction()).isEqualTo(TypeInfraction.RETARD_REUNION);

        verify(penaliteRepository).save(any(Penalite.class));
        verify(eventPublisher).publierPenaliteInfligee(any(Penalite.class));
    }

    @Test
    @DisplayName("Doit infliger une pénalité avec un montant spécifié manuellement")
    void doitInfligerPenaliteAvecMontantManuel() {
        when(penaliteRepository.save(any(Penalite.class)))
                .thenAnswer(invocation -> {
                    Penalite p = invocation.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });

        InfligerPenaliteRequest request = new InfligerPenaliteRequest(
                membreId, groupeId, sessionId, reunionId, TypeInfraction.TROUBLE_SEANCE, BigDecimal.valueOf(2500), "Sonnerie de téléphone en pleine séance"
        );

        PenaliteDto result = penaliteService.infligerPenalite(request);

        assertThat(result.montant()).isEqualByComparingTo(BigDecimal.valueOf(2500));
        assertThat(result.typeInfraction()).isEqualTo(TypeInfraction.TROUBLE_SEANCE);
        verify(tarificationRepository, never()).findByGroupeIdAndTypeInfractionAndActifTrue(any(), any());
    }

    @Test
    @DisplayName("Doit échouer si aucun barème n'existe et aucun montant n'est fourni")
    void doitEchouerSiPasDeTarif() {
        when(tarificationRepository.findByGroupeIdAndTypeInfractionAndActifTrue(groupeId, TypeInfraction.NON_RESPECT_REGLES))
                .thenReturn(Optional.empty());

        InfligerPenaliteRequest request = new InfligerPenaliteRequest(
                membreId, groupeId, sessionId, reunionId, TypeInfraction.NON_RESPECT_REGLES, null, "Tenue non conforme"
        );

        assertThatThrownBy(() -> penaliteService.infligerPenalite(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Aucun tarif actif");

        verify(penaliteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit enregistrer le paiement d'une pénalité")
    void doitEnregistrerPaiement() {
        UUID penaliteId = UUID.randomUUID();
        Penalite penalite = new Penalite(
                penaliteId, membreId, groupeId, sessionId, reunionId,
                TypeInfraction.RETARD_REUNION, BigDecimal.valueOf(1000), StatutPenalite.EN_ATTENTE, "Retard"
        );

        when(penaliteRepository.findById(penaliteId)).thenReturn(Optional.of(penalite));
        when(penaliteRepository.save(any(Penalite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PenaliteDto result = penaliteService.payer(penaliteId);

        assertThat(result.statut()).isEqualTo(StatutPenalite.PAYEE);
        assertThat(result.datePaiement()).isNotNull();
        verify(eventPublisher).publierPenalitePayee(penalite);
    }

    @Test
    @DisplayName("Doit échouer lors de la tentative de payer une pénalité déjà payée")
    void doitEchouerSiPaiementDejaEffectue() {
        UUID penaliteId = UUID.randomUUID();
        Penalite penalite = new Penalite(
                penaliteId, membreId, groupeId, sessionId, reunionId,
                TypeInfraction.RETARD_REUNION, BigDecimal.valueOf(1000), StatutPenalite.PAYEE, "Retard"
        );

        when(penaliteRepository.findById(penaliteId)).thenReturn(Optional.of(penalite));

        assertThatThrownBy(() -> penaliteService.payer(penaliteId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ne peut pas etre payee");

        verify(penaliteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit annuler une pénalité avec motif")
    void doitAnnulerPenalite() {
        UUID penaliteId = UUID.randomUUID();
        Penalite penalite = new Penalite(
                penaliteId, membreId, groupeId, sessionId, reunionId,
                TypeInfraction.ABSENCE, BigDecimal.valueOf(5000), StatutPenalite.EN_ATTENTE, "Absence"
        );

        when(penaliteRepository.findById(penaliteId)).thenReturn(Optional.of(penalite));
        when(penaliteRepository.save(any(Penalite.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PenaliteDto result = penaliteService.annuler(penaliteId, "Justificatif médical fourni ultérieurement");

        assertThat(result.statut()).isEqualTo(StatutPenalite.ANNULEE);
        assertThat(result.motif()).isEqualTo("Justificatif médical fourni ultérieurement");
        verify(eventPublisher).publierPenaliteAnnulee(penalite);
    }
}
