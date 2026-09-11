package com.njangi.statistiques.service;

import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.entity.StatistiqueGroupe;
import com.njangi.statistiques.exception.StatistiqueNotFoundException;
import com.njangi.statistiques.repository.StatistiqueGroupeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StatistiqueService {

    private final StatistiqueGroupeRepository statistiqueRepository;

    /**
     * Recalcule (ou crée) les statistiques pour un groupe et une session.
     * En pratique, cet agrégat est mis à jour via les événements Kafka
     * (cotisation.payee, reunion.terminee). Cette méthode permet un recalcul forcé.
     *
     * @param groupeId  identifiant du groupe
     * @param sessionId identifiant de la session
     * @return les statistiques recalculées
     */
    public StatistiqueGroupeDto calculer(UUID groupeId, UUID sessionId) {
        log.info("Recalcul des statistiques pour groupeId={} sessionId={}", groupeId, sessionId);

        StatistiqueGroupe stat = statistiqueRepository
                .findByGroupeIdAndSessionId(groupeId, sessionId)
                .orElseGet(() -> StatistiqueGroupe.builder()
                        .groupeId(groupeId)
                        .sessionId(sessionId)
                        .totalCollecte(BigDecimal.ZERO)
                        .nbMembres(0)
                        .tauxParticipation(BigDecimal.ZERO)
                        .tauxPresence(BigDecimal.ZERO)
                        .build());

        // TODO : récupérer les données réelles via ms-cotisations et ms-reunions (appels REST ou cache Kafka)
        // Exemple : stat.setTotalCollecte(cotisationClient.getTotalCollecte(groupeId, sessionId));
        // Exemple : stat.setTauxPresence(reunionClient.getTauxPresence(groupeId, sessionId));

        StatistiqueGroupe saved = statistiqueRepository.save(stat);
        log.info("Statistiques calculées pour groupeId={} sessionId={}", groupeId, sessionId);
        return StatistiqueGroupeDto.from(saved);
    }

    /**
     * Met à jour les statistiques à partir d'un événement Kafka.
     * Appelé par le consommateur Kafka.
     */
    public void mettreAJourDepuisEvenement(UUID groupeId, UUID sessionId,
                                            BigDecimal montantSupplementaire,
                                            boolean mettreAJourPresence) {
        log.debug("Mise à jour stats groupeId={} sessionId={} montant={} presence={}",
                groupeId, sessionId, montantSupplementaire, mettreAJourPresence);

        StatistiqueGroupe stat = statistiqueRepository
                .findByGroupeIdAndSessionId(groupeId, sessionId)
                .orElseGet(() -> StatistiqueGroupe.builder()
                        .groupeId(groupeId)
                        .sessionId(sessionId)
                        .totalCollecte(BigDecimal.ZERO)
                        .nbMembres(0)
                        .tauxParticipation(BigDecimal.ZERO)
                        .tauxPresence(BigDecimal.ZERO)
                        .build());

        if (montantSupplementaire != null && montantSupplementaire.compareTo(BigDecimal.ZERO) > 0) {
            stat.setTotalCollecte(stat.getTotalCollecte().add(montantSupplementaire));
        }

        statistiqueRepository.save(stat);
    }

    @Transactional(readOnly = true)
    public List<StatistiqueGroupeDto> findByGroupe(UUID groupeId) {
        return statistiqueRepository.findByGroupeIdOrderByCalculeLe(groupeId)
                .stream().map(StatistiqueGroupeDto::from).toList();
    }

    @Transactional(readOnly = true)
    public StatistiqueGroupeDto findByGroupeAndSession(UUID groupeId, UUID sessionId) {
        return statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId)
                .map(StatistiqueGroupeDto::from)
                .orElseThrow(() -> new StatistiqueNotFoundException(groupeId, sessionId));
    }
}
