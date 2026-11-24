package com.njangi.finances.cotisation.service;

import com.njangi.finances.cotisation.dto.CotisationDto;
import com.njangi.finances.cotisation.dto.GenererCotisationsReunionRequest;
import com.njangi.finances.cotisation.dto.MettreAJourPaiementCotisationRequest;
import com.njangi.finances.cotisation.entity.CategorieCotisation;
import com.njangi.finances.cotisation.entity.Cotisation;
import com.njangi.finances.cotisation.entity.StatutCotisation;
import com.njangi.finances.cotisation.entity.TypeCotisation;
import com.njangi.finances.cotisation.repository.CotisationRepository;
import com.njangi.finances.cotisation.repository.TypeCotisationRepository;
import com.njangi.finances.event.FinancesEventPublisher;
import com.njangi.finances.penalite.dto.PenaliteDto;
import com.njangi.finances.penalite.entity.StatutPenalite;
import com.njangi.finances.penalite.entity.TypeInfraction;
import com.njangi.finances.penalite.service.PenaliteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CotisationServiceTest {

    @Mock
    private CotisationRepository cotisationRepository;

    @Mock
    private TypeCotisationRepository typeCotisationRepository;

    @Mock
    private PenaliteService penaliteService;

    @Mock
    private FinancesEventPublisher eventPublisher;

    @InjectMocks
    private CotisationService cotisationService;

    private UUID groupeId;
    private UUID reunionId;
    private UUID membreId1;
    private UUID membreId2;

    @BeforeEach
    void setUp() {
        groupeId = UUID.randomUUID();
        reunionId = UUID.randomUUID();
        membreId1 = UUID.randomUUID();
        membreId2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit générer automatiquement les cotisations obligatoires pour une réunion")
    void doitGenererCotisationsPourReunion() {
        TypeCotisation potType = new TypeCotisation(UUID.randomUUID(), groupeId, "Pot Rotatif", null,
                CategorieCotisation.ROTATIVE_POT, BigDecimal.valueOf(50000), true, true, "ACTIF");
        TypeCotisation secoursType = new TypeCotisation(UUID.randomUUID(), groupeId, "Secours", null,
                CategorieCotisation.SECOURS_DECES, BigDecimal.valueOf(5000), false, true, "ACTIF");

        when(typeCotisationRepository.findByGroupeIdAndEstObligatoireTrueAndStatut(groupeId, "ACTIF"))
                .thenReturn(List.of(potType, secoursType));

        when(cotisationRepository.findByReunionIdAndMembreIdAndTypeCotisationId(any(), any(), any()))
                .thenReturn(Optional.empty());

        when(cotisationRepository.save(any(Cotisation.class)))
                .thenAnswer(invocation -> {
                    Cotisation c = invocation.getArgument(0);
                    c.setId(UUID.randomUUID());
                    return c;
                });

        GenererCotisationsReunionRequest request = new GenererCotisationsReunionRequest(
                groupeId,
                reunionId,
                List.of(membreId1, membreId2),
                LocalDate.now().plusDays(7)
        );

        List<CotisationDto> result = cotisationService.genererCotisationsPourReunion(request);

        assertThat(result).hasSize(4);
        verify(cotisationRepository, times(4)).save(any(Cotisation.class));
        verify(eventPublisher, times(4)).publierCotisationCreee(any(), any(), eq(groupeId), eq(reunionId), any());
    }

    @Test
    @DisplayName("Doit enregistrer un paiement partiel")
    void doitEnregistrerPaiementPartiel() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.ZERO, StatutCotisation.EN_ATTENTE, LocalDate.now()
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MettreAJourPaiementCotisationRequest request = new MettreAJourPaiementCotisationRequest(BigDecimal.valueOf(20000));

        CotisationDto result = cotisationService.enregistrerPaiement(cotisationId, request);

        assertThat(result.statut()).isEqualTo(StatutCotisation.PARTIEL);
        assertThat(result.montantPaye()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        verify(eventPublisher, never()).publierCotisationPayee(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Doit solder une cotisation en totalité et publier l'événement Kafka")
    void doitSolderCotisationTotalement() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.valueOf(20000), StatutCotisation.PARTIEL, LocalDate.now()
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MettreAJourPaiementCotisationRequest request = new MettreAJourPaiementCotisationRequest(BigDecimal.valueOf(30000));

        CotisationDto result = cotisationService.enregistrerPaiement(cotisationId, request);

        assertThat(result.statut()).isEqualTo(StatutCotisation.PAYE);
        assertThat(result.montantPaye()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(result.datePaiement()).isNotNull();

        verify(eventPublisher).publierCotisationPayee(
                eq(cotisationId), eq(membreId1), eq(groupeId), eq(reunionId), eq(BigDecimal.valueOf(50000)));
    }

    @Test
    @DisplayName("Doit appliquer un paiement ACID sur une cotisation existante ou retourner vide si absente")
    void doitAppliquerPaiementSiExistante() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.ZERO, StatutCotisation.EN_ATTENTE, LocalDate.now()
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<CotisationDto> updated = cotisationService.appliquerPaiementSiExistante(cotisationId, BigDecimal.valueOf(50000));

        assertThat(updated).isPresent();
        assertThat(updated.get().statut()).isEqualTo(StatutCotisation.PAYE);
        verify(eventPublisher).publierCotisationPayee(
                eq(cotisationId), eq(membreId1), eq(groupeId), eq(reunionId), eq(BigDecimal.valueOf(50000)));

        UUID absenteId = UUID.randomUUID();
        when(cotisationRepository.findById(absenteId)).thenReturn(Optional.empty());
        assertThat(cotisationService.appliquerPaiementSiExistante(absenteId, BigDecimal.valueOf(10000))).isEmpty();
    }

    @Test
    @DisplayName("Doit marquer une cotisation impayée en retard sans pénalité si aucun tarif configuré")
    void doitMarquerCotisationEnRetard() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.ZERO, StatutCotisation.EN_ATTENTE, LocalDate.now().minusDays(1)
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(penaliteService.appliquerPenaliteRetardSiTarifConfigure(membreId1, groupeId, null, reunionId, cotisationId))
                .thenReturn(Optional.empty());

        CotisationDto result = cotisationService.marquerEnRetard(cotisationId);

        assertThat(result.statut()).isEqualTo(StatutCotisation.EN_RETARD);
        verify(cotisationRepository).save(cotisation);
        verify(eventPublisher).publierCotisationRetard(
                eq(cotisationId), eq(membreId1), eq(groupeId), eq(reunionId), eq(BigDecimal.valueOf(50000)));
    }

    @Test
    @DisplayName("Doit marquer une cotisation en retard et appliquer automatiquement la pénalité ACID si un tarif est configuré")
    void doitMarquerCotisationEnRetardEtAppliquerPenaliteAutomatique() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.ZERO, StatutCotisation.EN_ATTENTE, LocalDate.now().minusDays(2)
        );
        PenaliteDto penaliteCreee = new PenaliteDto(
                UUID.randomUUID(), membreId1, groupeId, reunionId, reunionId,
                TypeInfraction.RETARD_PAIEMENT, BigDecimal.valueOf(2000), StatutPenalite.EN_ATTENTE,
                "Pénalité automatique", null, LocalDateTime.now()
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(penaliteService.appliquerPenaliteRetardSiTarifConfigure(membreId1, groupeId, null, reunionId, cotisationId))
                .thenReturn(Optional.of(penaliteCreee));

        CotisationDto result = cotisationService.marquerEnRetard(cotisationId);

        assertThat(result.statut()).isEqualTo(StatutCotisation.PENALISE);
        verify(penaliteService).appliquerPenaliteRetardSiTarifConfigure(membreId1, groupeId, null, reunionId, cotisationId);
        verify(eventPublisher).publierCotisationRetard(
                eq(cotisationId), eq(membreId1), eq(groupeId), eq(reunionId), eq(BigDecimal.valueOf(50000)));
    }
}
