package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.AttribuerPotRequest;
import com.njangi.cotisations.dto.DecaisserPotRequest;
import com.njangi.cotisations.dto.PlanifierTourPotRequest;
import com.njangi.cotisations.dto.PotSessionDto;
import com.njangi.cotisations.entity.ModeVersement;
import com.njangi.cotisations.entity.PotSession;
import com.njangi.cotisations.entity.StatutPot;
import com.njangi.cotisations.event.CotisationEventPublisher;
import com.njangi.cotisations.exception.BusinessException;
import com.njangi.cotisations.repository.PotSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PotSessionServiceTest {

    @Mock
    private PotSessionRepository potSessionRepository;

    @Mock
    private CotisationEventPublisher eventPublisher;

    @InjectMocks
    private PotSessionService potSessionService;

    private UUID groupeId;
    private UUID sessionId;
    private UUID membreId1;
    private UUID membreId2;
    private UUID membreId3;

    @BeforeEach
    void setUp() {
        groupeId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        membreId1 = UUID.randomUUID();
        membreId2 = UUID.randomUUID();
        membreId3 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit planifier le tour de passage du pot pour une session")
    void doitPlanifierTourPot() {
        PlanifierTourPotRequest request = new PlanifierTourPotRequest(
                groupeId,
                sessionId,
                List.of(membreId1, membreId2, membreId3),
                BigDecimal.valueOf(500000)
        );

        when(potSessionRepository.save(any(PotSession.class)))
                .thenAnswer(invocation -> {
                    PotSession p = invocation.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });

        List<PotSessionDto> result = potSessionService.planifierTourPot(request);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).ordrePassage()).isEqualTo(1);
        assertThat(result.get(0).membreBeneficiaireId()).isEqualTo(membreId1);
        assertThat(result.get(0).statut()).isEqualTo(StatutPot.PLANIFIE);

        assertThat(result.get(1).ordrePassage()).isEqualTo(2);
        assertThat(result.get(1).membreBeneficiaireId()).isEqualTo(membreId2);

        assertThat(result.get(2).ordrePassage()).isEqualTo(3);
        assertThat(result.get(2).membreBeneficiaireId()).isEqualTo(membreId3);

        verify(potSessionRepository, times(3)).save(any(PotSession.class));
    }

    @Test
    @DisplayName("Doit attribuer le pot à un bénéficiaire lors d'une réunion")
    void doitAttribuerPot() {
        UUID potId = UUID.randomUUID();
        UUID reunionId = UUID.randomUUID();

        PotSession pot = new PotSession(
                potId, groupeId, sessionId, null, membreId1,
                1, BigDecimal.valueOf(500000), BigDecimal.valueOf(500000), StatutPot.PLANIFIE
        );

        when(potSessionRepository.findById(potId)).thenReturn(Optional.of(pot));
        when(potSessionRepository.save(any(PotSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttribuerPotRequest request = new AttribuerPotRequest(reunionId, BigDecimal.valueOf(490000));

        PotSessionDto result = potSessionService.attribuerPot(potId, request);

        assertThat(result.statut()).isEqualTo(StatutPot.ATTRIBUE);
        assertThat(result.reunionId()).isEqualTo(reunionId);
        assertThat(result.montantNet()).isEqualByComparingTo(BigDecimal.valueOf(490000));
        assertThat(result.dateAttribution()).isNotNull();

        verify(eventPublisher).publierPotAttribue(eq(potId), eq(groupeId), eq(membreId1), any());
    }

    @Test
    @DisplayName("Doit enregistrer le décaissement du pot (Cash / Mobile Money)")
    void doitDecaisserPot() {
        UUID potId = UUID.randomUUID();
        UUID reunionId = UUID.randomUUID();

        PotSession pot = new PotSession(
                potId, groupeId, sessionId, reunionId, membreId1,
                1, BigDecimal.valueOf(500000), BigDecimal.valueOf(500000), StatutPot.ATTRIBUE
        );

        when(potSessionRepository.findById(potId)).thenReturn(Optional.of(pot));
        when(potSessionRepository.save(any(PotSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DecaisserPotRequest request = new DecaisserPotRequest(
                ModeVersement.MTN_MOMO,
                "TRX-MOMO-2026-9988",
                BigDecimal.valueOf(500000)
        );

        PotSessionDto result = potSessionService.decaisserPot(potId, request);

        assertThat(result.statut()).isEqualTo(StatutPot.DECAISSE);
        assertThat(result.modeVersement()).isEqualTo(ModeVersement.MTN_MOMO);
        assertThat(result.referencePaiement()).isEqualTo("TRX-MOMO-2026-9988");
        assertThat(result.dateVersement()).isNotNull();

        verify(eventPublisher).publierPotVerse(eq(potId), eq(groupeId), eq(membreId1), eq(BigDecimal.valueOf(500000)));
    }

    @Test
    @DisplayName("Doit lever une exception si le pot a déjà été décaissé")
    void doitEchouerSiPotDejaDecaisse() {
        UUID potId = UUID.randomUUID();

        PotSession pot = new PotSession(
                potId, groupeId, sessionId, UUID.randomUUID(), membreId1,
                1, BigDecimal.valueOf(500000), BigDecimal.valueOf(500000), StatutPot.DECAISSE
        );

        when(potSessionRepository.findById(potId)).thenReturn(Optional.of(pot));

        DecaisserPotRequest request = new DecaisserPotRequest(ModeVersement.CASH, "RECU-123", BigDecimal.valueOf(500000));

        assertThatThrownBy(() -> potSessionService.decaisserPot(potId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ce pot a deja ete decaisse");

        verify(potSessionRepository, never()).save(any());
    }
}
