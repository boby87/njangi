package com.njangi.penalites.service;

import com.njangi.penalites.dto.DefinirTarifRequest;
import com.njangi.penalites.dto.TarificationPenaliteDto;
import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import com.njangi.penalites.repository.TarificationPenaliteRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarificationServiceTest {

    @Mock
    private TarificationPenaliteRepository tarificationRepository;

    @InjectMocks
    private TarificationService tarificationService;

    private UUID groupeId;

    @BeforeEach
    void setUp() {
        groupeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit créer un nouveau tarif d'amende pour un groupe")
    void doitCreerNouveauTarif() {
        DefinirTarifRequest request = new DefinirTarifRequest(
                groupeId,
                TypeInfraction.RETARD_REUNION,
                BigDecimal.valueOf(1000),
                true
        );

        when(tarificationRepository.findByGroupeIdAndTypeInfraction(groupeId, TypeInfraction.RETARD_REUNION))
                .thenReturn(Optional.empty());

        when(tarificationRepository.save(any(TarificationPenalite.class)))
                .thenAnswer(invocation -> {
                    TarificationPenalite t = invocation.getArgument(0);
                    t.setId(UUID.randomUUID());
                    return t;
                });

        TarificationPenaliteDto result = tarificationService.definirOuMettreAJourTarif(request);

        assertThat(result).isNotNull();
        assertThat(result.groupeId()).isEqualTo(groupeId);
        assertThat(result.typeInfraction()).isEqualTo(TypeInfraction.RETARD_REUNION);
        assertThat(result.montant()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(result.actif()).isTrue();

        verify(tarificationRepository).save(any(TarificationPenalite.class));
    }

    @Test
    @DisplayName("Doit mettre à jour un tarif existant pour un groupe")
    void doitMettreAJourTarifExistant() {
        UUID tarifId = UUID.randomUUID();
        TarificationPenalite existant = new TarificationPenalite(
                tarifId, groupeId, TypeInfraction.ABSENCE, BigDecimal.valueOf(3000), true
        );

        when(tarificationRepository.findByGroupeIdAndTypeInfraction(groupeId, TypeInfraction.ABSENCE))
                .thenReturn(Optional.of(existant));

        when(tarificationRepository.save(any(TarificationPenalite.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DefinirTarifRequest request = new DefinirTarifRequest(
                groupeId, TypeInfraction.ABSENCE, BigDecimal.valueOf(5000), true
        );

        TarificationPenaliteDto result = tarificationService.definirOuMettreAJourTarif(request);

        assertThat(result.montant()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(existant.getMontant()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(tarificationRepository).save(existant);
    }

    @Test
    @DisplayName("Doit lister les tarifs actifs d'un groupe")
    void doitListerTarifsActifs() {
        TarificationPenalite t1 = new TarificationPenalite(UUID.randomUUID(), groupeId, TypeInfraction.RETARD_REUNION, BigDecimal.valueOf(1000), true);
        TarificationPenalite t2 = new TarificationPenalite(UUID.randomUUID(), groupeId, TypeInfraction.ABSENCE, BigDecimal.valueOf(5000), true);

        when(tarificationRepository.findByGroupeIdAndActifTrue(groupeId))
                .thenReturn(List.of(t1, t2));

        List<TarificationPenaliteDto> result = tarificationService.obtenirTarifsActifsParGroupe(groupeId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).typeInfraction()).isEqualTo(TypeInfraction.RETARD_REUNION);
        assertThat(result.get(1).typeInfraction()).isEqualTo(TypeInfraction.ABSENCE);
    }
}
