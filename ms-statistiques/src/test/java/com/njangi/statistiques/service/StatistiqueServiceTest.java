package com.njangi.statistiques.service;

import com.njangi.statistiques.dto.BilanFinancierDto;
import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.entity.StatistiqueGroupe;
import com.njangi.statistiques.event.StatistiqueEventPublisher;
import com.njangi.statistiques.exception.StatistiqueNotFoundException;
import com.njangi.statistiques.repository.StatistiqueGroupeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatistiqueServiceTest {

    @Mock
    private StatistiqueGroupeRepository statistiqueRepository;

    @Mock
    private StatistiqueEventPublisher eventPublisher;

    private StatistiqueService statistiqueService;

    private UUID groupeId;
    private UUID sessionId;

    @BeforeEach
    void setUp() {
        statistiqueService = new StatistiqueService(statistiqueRepository, eventPublisher);
        groupeId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Calcul forcé des statistiques et publication Kafka")
    void testCalculer() {
        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.empty());

        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> {
                    StatistiqueGroupe s = inv.getArgument(0);
                    s.setId(UUID.randomUUID());
                    return s;
                });

        StatistiqueGroupeDto result = statistiqueService.calculer(groupeId, sessionId);

        assertThat(result).isNotNull();
        assertThat(result.groupeId()).isEqualTo(groupeId);
        assertThat(result.sessionId()).isEqualTo(sessionId);
        assertThat(result.totalCollecte()).isEqualTo(BigDecimal.ZERO);
        verify(statistiqueRepository).save(any(StatistiqueGroupe.class));
        verify(eventPublisher).publishStatistiqueCalculee(any(StatistiqueGroupe.class));
    }

    @Test
    @DisplayName("Enregistrement d'une cotisation en Cash")
    void testEnregistrerCotisationCash() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));
        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        statistiqueService.enregistrerCotisation(groupeId, sessionId, BigDecimal.valueOf(50000), "CASH");

        assertThat(stat.getTotalCollecte()).isEqualByComparingTo("50000");
        assertThat(stat.getTotalCash()).isEqualByComparingTo("50000");
        assertThat(stat.getTotalMomo()).isEqualByComparingTo("0");
        assertThat(stat.getSoldeCaisse()).isEqualByComparingTo("50000");
        verify(statistiqueRepository).save(stat);
    }

    @Test
    @DisplayName("Enregistrement d'une cotisation en Mobile Money")
    void testEnregistrerCotisationMomo() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));
        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        statistiqueService.enregistrerCotisation(groupeId, sessionId, BigDecimal.valueOf(25000), "MTN_MOMO");

        assertThat(stat.getTotalCollecte()).isEqualByComparingTo("25000");
        assertThat(stat.getTotalMomo()).isEqualByComparingTo("25000");
        assertThat(stat.getTotalCash()).isEqualByComparingTo("0");
        assertThat(stat.getSoldeCaisse()).isEqualByComparingTo("25000");
    }

    @Test
    @DisplayName("Enregistrement d'un décaissement de pot (diminue le solde de caisse)")
    void testEnregistrerDecaissement() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);
        stat.setTotalCollecte(BigDecimal.valueOf(100000));
        stat.setSoldeCaisse(BigDecimal.valueOf(100000));

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));
        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        statistiqueService.enregistrerDecaissement(groupeId, sessionId, BigDecimal.valueOf(80000));

        assertThat(stat.getTotalDecaisse()).isEqualByComparingTo("80000");
        assertThat(stat.getSoldeCaisse()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("Enregistrement d'une amende/pénalité (augmente le solde de caisse)")
    void testEnregistrerPenalite() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));
        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        statistiqueService.enregistrerPenalite(groupeId, sessionId, BigDecimal.valueOf(5000));

        assertThat(stat.getTotalPenalites()).isEqualByComparingTo("5000");
        assertThat(stat.getSoldeCaisse()).isEqualByComparingTo("5000");
    }

    @Test
    @DisplayName("Enregistrement de réunion et calcul du taux de présence")
    void testEnregistrerReunion() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);
        stat.setNbReunions(0);

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));
        when(statistiqueRepository.save(any(StatistiqueGroupe.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // 8 présents sur 10 membres = 80.00%
        statistiqueService.enregistrerReunion(groupeId, sessionId, 8, 10);

        assertThat(stat.getNbReunions()).isEqualTo(1);
        assertThat(stat.getTauxPresence()).isEqualByComparingTo("80.00");
    }

    @Test
    @DisplayName("Génération du bilan financier consolidé")
    void testGenererBilanFinancier() {
        StatistiqueGroupe stat = new StatistiqueGroupe();
        stat.setGroupeId(groupeId);
        stat.setSessionId(sessionId);
        stat.setTotalCollecte(BigDecimal.valueOf(200000));
        stat.setTotalDecaisse(BigDecimal.valueOf(150000));
        stat.setTotalPenalites(BigDecimal.valueOf(10000));
        stat.setTotalCash(BigDecimal.valueOf(100000));
        stat.setTotalMomo(BigDecimal.valueOf(100000));
        stat.setSoldeCaisse(BigDecimal.valueOf(60000));
        stat.setNbMembres(12);
        stat.setNbReunions(3);
        stat.setTauxPresence(BigDecimal.valueOf(95.00));
        stat.setTauxParticipation(BigDecimal.valueOf(100.00));

        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.of(stat));

        BilanFinancierDto bilan = statistiqueService.genererBilanFinancier(groupeId, sessionId);

        assertThat(bilan).isNotNull();
        assertThat(bilan.soldeGlobalXAF()).isEqualByComparingTo("60000");
        assertThat(bilan.totalCotisationsXAF()).isEqualByComparingTo("200000");
        assertThat(bilan.totalDecaissementsXAF()).isEqualByComparingTo("150000");
        assertThat(bilan.statutSanteFinanciere()).isEqualTo("EXCELLENTE");
        assertThat(bilan.repartitionTresorerie().get("CASH")).isEqualByComparingTo("100000");
        assertThat(bilan.repartitionTresorerie().get("MOBILE_MONEY")).isEqualByComparingTo("100000");
    }

    @Test
    @DisplayName("Statistique non trouvée lève StatistiqueNotFoundException")
    void testFindByGroupeAndSessionNotFound() {
        when(statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> statistiqueService.findByGroupeAndSession(groupeId, sessionId))
                .isInstanceOf(StatistiqueNotFoundException.class);
    }
}
