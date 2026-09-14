package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.CotisationDto;
import com.njangi.cotisations.dto.GenererCotisationsReunionRequest;
import com.njangi.cotisations.dto.MettreAJourPaiementCotisationRequest;
import com.njangi.cotisations.entity.CategorieCotisation;
import com.njangi.cotisations.entity.Cotisation;
import com.njangi.cotisations.entity.StatutCotisation;
import com.njangi.cotisations.entity.TypeCotisation;
import com.njangi.cotisations.event.CotisationEventPublisher;
import com.njangi.cotisations.repository.CotisationRepository;
import com.njangi.cotisations.repository.TypeCotisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private CotisationEventPublisher eventPublisher;

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

        // 2 membres x 2 types obligatoires = 4 cotisations générées
        assertThat(result).hasSize(4);
        verify(cotisationRepository, times(4)).save(any(Cotisation.class));
        verify(eventPublisher, times(4)).publierCotisationGeneree(any(), any(), eq(groupeId), any());
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
        verify(eventPublisher, never()).publierCotisationPayee(any(), any(), any(), any());
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

        verify(eventPublisher).publierCotisationPayee(eq(cotisationId), eq(membreId1), eq(groupeId), eq(BigDecimal.valueOf(50000)));
    }

    @Test
    @DisplayName("Doit marquer une cotisation impayée en retard")
    void doitMarquerCotisationEnRetard() {
        UUID cotisationId = UUID.randomUUID();
        Cotisation cotisation = new Cotisation(
                cotisationId, groupeId, membreId1, UUID.randomUUID(), reunionId,
                BigDecimal.valueOf(50000), BigDecimal.ZERO, StatutCotisation.EN_ATTENTE, LocalDate.now().minusDays(1)
        );

        when(cotisationRepository.findById(cotisationId)).thenReturn(Optional.of(cotisation));
        when(cotisationRepository.save(any(Cotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CotisationDto result = cotisationService.marquerEnRetard(cotisationId);

        assertThat(result.statut()).isEqualTo(StatutCotisation.EN_RETARD);
        verify(cotisationRepository).save(cotisation);
    }
}
