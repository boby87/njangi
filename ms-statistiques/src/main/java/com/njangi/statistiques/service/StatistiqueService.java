package com.njangi.statistiques.service;

import com.njangi.statistiques.dto.BilanFinancierDto;
import com.njangi.statistiques.dto.StatistiqueGroupeDto;
import com.njangi.statistiques.entity.StatistiqueGroupe;
import com.njangi.statistiques.event.StatistiqueEventPublisher;
import com.njangi.statistiques.exception.StatistiqueNotFoundException;
import com.njangi.statistiques.repository.StatistiqueGroupeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class StatistiqueService {

    private static final Logger log = LoggerFactory.getLogger(StatistiqueService.class);

    private final StatistiqueGroupeRepository statistiqueRepository;
    private final StatistiqueEventPublisher eventPublisher;

    public StatistiqueService(StatistiqueGroupeRepository statistiqueRepository,
                              StatistiqueEventPublisher eventPublisher) {
        this.statistiqueRepository = statistiqueRepository;
        this.eventPublisher = eventPublisher;
    }

    public StatistiqueGroupeDto calculer(UUID groupeId, UUID sessionId) {
        log.info("Recalcul forcé des statistiques pour groupeId={} sessionId={}", groupeId, sessionId);
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);
        recalculerSolde(stat);
        StatistiqueGroupe saved = statistiqueRepository.save(stat);

        try {
            eventPublisher.publishStatistiqueCalculee(saved);
        } catch (Exception e) {
            log.warn("Impossible de publier statistique.calculee pour groupeId={} : {}", groupeId, e.getMessage());
        }

        return StatistiqueGroupeDto.from(saved);
    }

    public void enregistrerCotisation(UUID groupeId, UUID sessionId, BigDecimal montant, String modePaiement) {
        log.info("Agrégation cotisation groupeId={} sessionId={} montant={} mode={}",
                groupeId, sessionId, montant, modePaiement);
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);

        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            stat.setTotalCollecte(stat.getTotalCollecte().add(montant));

            if (modePaiement != null && modePaiement.equalsIgnoreCase("CASH")) {
                stat.setTotalCash(stat.getTotalCash().add(montant));
            } else if (modePaiement != null && (modePaiement.contains("MOMO") || modePaiement.contains("ORANGE") || modePaiement.contains("MTN"))) {
                stat.setTotalMomo(stat.getTotalMomo().add(montant));
            }
        }

        recalculerSolde(stat);
        statistiqueRepository.save(stat);
    }

    public void enregistrerDecaissement(UUID groupeId, UUID sessionId, BigDecimal montant) {
        log.info("Agrégation décaissement pot/secours groupeId={} sessionId={} montant={}",
                groupeId, sessionId, montant);
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);

        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            stat.setTotalDecaisse(stat.getTotalDecaisse().add(montant));
        }

        recalculerSolde(stat);
        statistiqueRepository.save(stat);
    }

    public void enregistrerPenalite(UUID groupeId, UUID sessionId, BigDecimal montant) {
        log.info("Agrégation pénalité groupeId={} sessionId={} montant={}", groupeId, sessionId, montant);
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);

        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            stat.setTotalPenalites(stat.getTotalPenalites().add(montant));
        }

        recalculerSolde(stat);
        statistiqueRepository.save(stat);
    }

    public void enregistrerReunion(UUID groupeId, UUID sessionId, int nbPresents, int nbTotalMembres) {
        log.info("Agrégation réunion groupeId={} sessionId={} presents={}/{}",
                groupeId, sessionId, nbPresents, nbTotalMembres);
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);
        stat.setNbReunions(stat.getNbReunions() + 1);

        if (nbTotalMembres > 0) {
            BigDecimal tauxSeance = BigDecimal.valueOf(nbPresents)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(nbTotalMembres), 2, RoundingMode.HALF_UP);

            // Moyenne lissée avec le nombre de réunions
            if (stat.getNbReunions() == 1) {
                stat.setTauxPresence(tauxSeance);
            } else {
                BigDecimal totalTauxPrecedent = stat.getTauxPresence().multiply(BigDecimal.valueOf(stat.getNbReunions() - 1));
                BigDecimal nouveauTauxMoyen = totalTauxPrecedent.add(tauxSeance)
                        .divide(BigDecimal.valueOf(stat.getNbReunions()), 2, RoundingMode.HALF_UP);
                stat.setTauxPresence(nouveauTauxMoyen);
            }
        }

        recalculerSolde(stat);
        statistiqueRepository.save(stat);
    }

    public void enregistrerMembre(UUID groupeId, UUID sessionId) {
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);
        stat.setNbMembres(stat.getNbMembres() + 1);
        statistiqueRepository.save(stat);
    }

    @Transactional(readOnly = true)
    public BilanFinancierDto genererBilanFinancier(UUID groupeId, UUID sessionId) {
        StatistiqueGroupe stat = findOrCreate(groupeId, sessionId);

        Map<String, BigDecimal> tresorerie = new HashMap<>();
        tresorerie.put("CASH", stat.getTotalCash());
        tresorerie.put("MOBILE_MONEY", stat.getTotalMomo());
        tresorerie.put("TOTAL_ENCAISSE", stat.getTotalCash().add(stat.getTotalMomo()));

        // Évaluation de la santé financière
        String sante = "EXCELLENTE";
        if (stat.getSoldeCaisse().compareTo(BigDecimal.ZERO) < 0) {
            sante = "DÉFICITAIRE";
        } else if (stat.getTauxPresence().compareTo(BigDecimal.valueOf(60)) < 0) {
            sante = "ATTENTION_ASSIDUITE_FAIBLE";
        }

        return new BilanFinancierDto(
                groupeId,
                sessionId,
                stat.getSoldeCaisse(),
                stat.getTotalCollecte(),
                stat.getTotalDecaisse(),
                stat.getTotalPenalites(),
                tresorerie,
                stat.getTauxParticipation(),
                stat.getTauxPresence(),
                sante,
                stat.getNbMembres(),
                stat.getNbReunions(),
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public List<StatistiqueGroupeDto> findByGroupe(UUID groupeId) {
        return statistiqueRepository.findByGroupeIdOrderByCalculeLeDesc(groupeId)
                .stream().map(StatistiqueGroupeDto::from).toList();
    }

    @Transactional(readOnly = true)
    public StatistiqueGroupeDto findByGroupeAndSession(UUID groupeId, UUID sessionId) {
        return statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId)
                .map(StatistiqueGroupeDto::from)
                .orElseThrow(() -> new StatistiqueNotFoundException(groupeId, sessionId));
    }

    private StatistiqueGroupe findOrCreate(UUID groupeId, UUID sessionId) {
        return statistiqueRepository.findByGroupeIdAndSessionId(groupeId, sessionId)
                .orElseGet(() -> {
                    StatistiqueGroupe s = new StatistiqueGroupe();
                    s.setGroupeId(groupeId);
                    s.setSessionId(sessionId);
                    s.setTotalCollecte(BigDecimal.ZERO);
                    s.setTotalDecaisse(BigDecimal.ZERO);
                    s.setSoldeCaisse(BigDecimal.ZERO);
                    s.setTotalPenalites(BigDecimal.ZERO);
                    s.setTotalCash(BigDecimal.ZERO);
                    s.setTotalMomo(BigDecimal.ZERO);
                    s.setNbMembres(0);
                    s.setNbReunions(0);
                    s.setTauxParticipation(BigDecimal.valueOf(100)); // Initialisé à 100%
                    s.setTauxPresence(BigDecimal.valueOf(100));
                    s.setCalculeLe(LocalDateTime.now());
                    return s;
                });
    }

    private void recalculerSolde(StatistiqueGroupe stat) {
        stat.setSoldeCaisse(stat.getTotalCollecte().add(stat.getTotalPenalites()).subtract(stat.getTotalDecaisse()));
        stat.setCalculeLe(LocalDateTime.now());
    }
}
