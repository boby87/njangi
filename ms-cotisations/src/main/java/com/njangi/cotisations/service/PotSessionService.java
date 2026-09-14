package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.AttribuerPotRequest;
import com.njangi.cotisations.dto.DecaisserPotRequest;
import com.njangi.cotisations.dto.PlanifierTourPotRequest;
import com.njangi.cotisations.dto.PotSessionDto;
import com.njangi.cotisations.entity.PotSession;
import com.njangi.cotisations.entity.StatutPot;
import com.njangi.cotisations.event.CotisationEventPublisher;
import com.njangi.cotisations.exception.BusinessException;
import com.njangi.cotisations.exception.NotFoundException;
import com.njangi.cotisations.repository.PotSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PotSessionService {

    private static final Logger log = LoggerFactory.getLogger(PotSessionService.class);

    private final PotSessionRepository potSessionRepository;
    private final CotisationEventPublisher eventPublisher;

    public PotSessionService(PotSessionRepository potSessionRepository,
                             CotisationEventPublisher eventPublisher) {
        this.potSessionRepository = potSessionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<PotSessionDto> planifierTourPot(PlanifierTourPotRequest request) {
        List<PotSession> tours = new ArrayList<>();
        List<UUID> beneficiaires = request.ordreBeneficiaires();

        for (int i = 0; i < beneficiaires.size(); i++) {
            UUID membreId = beneficiaires.get(i);
            int ordre = i + 1;

            PotSession pot = new PotSession(
                    null,
                    request.groupeId(),
                    request.sessionTontineId(),
                    null,
                    membreId,
                    ordre,
                    request.montantParTour(),
                    request.montantParTour(),
                    StatutPot.PLANIFIE
            );
            tours.add(potSessionRepository.save(pot));
        }

        log.info("{} tours de pot planifies pour la session {}", tours.size(), request.sessionTontineId());
        return tours.stream().map(this::toDto).toList();
    }

    public PotSessionDto attribuerPot(UUID potSessionId, AttribuerPotRequest request) {
        PotSession pot = potSessionRepository.findById(potSessionId)
                .orElseThrow(() -> new NotFoundException("Tour de pot non trouve : " + potSessionId));

        if (pot.getStatut() == StatutPot.DECAISSE) {
            throw new BusinessException("Ce pot a deja ete decaisse");
        }

        pot.setReunionId(request.reunionId());
        if (request.montantAjuste() != null) {
            pot.setMontantNet(request.montantAjuste());
        }
        pot.setStatut(StatutPot.ATTRIBUE);
        pot.setDateAttribution(LocalDateTime.now());

        PotSession updated = potSessionRepository.save(pot);
        eventPublisher.publierPotAttribue(updated.getId(), updated.getGroupeId(),
                updated.getMembreBeneficiaireId(), updated.getMontantTotal());

        log.info("Pot {} attribue au membre {} pour la reunion {}",
                potSessionId, updated.getMembreBeneficiaireId(), request.reunionId());
        return toDto(updated);
    }

    public PotSessionDto decaisserPot(UUID potSessionId, DecaisserPotRequest request) {
        PotSession pot = potSessionRepository.findById(potSessionId)
                .orElseThrow(() -> new NotFoundException("Tour de pot non trouve : " + potSessionId));

        if (pot.getStatut() == StatutPot.DECAISSE) {
            throw new BusinessException("Ce pot a deja ete decaisse");
        }

        pot.setModeVersement(request.modeVersement());
        pot.setReferencePaiement(request.referencePaiement());
        if (request.montantNet() != null) {
            pot.setMontantNet(request.montantNet());
        }
        pot.setStatut(StatutPot.DECAISSE);
        pot.setDateVersement(LocalDateTime.now());

        PotSession updated = potSessionRepository.save(pot);
        eventPublisher.publierPotVerse(updated.getId(), updated.getGroupeId(),
                updated.getMembreBeneficiaireId(), updated.getMontantNet());

        log.info("Pot {} decaisse avec succes : beneficiaire={}, mode={}, montant={}",
                potSessionId, updated.getMembreBeneficiaireId(), request.modeVersement(), updated.getMontantNet());
        return toDto(updated);
    }

    @Transactional(readOnly = true)
    public List<PotSessionDto> obtenirPotsParSession(UUID sessionTontineId) {
        return potSessionRepository.findBySessionTontineIdOrderByOrdrePassageAsc(sessionTontineId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PotSessionDto> obtenirPotsParGroupe(UUID groupeId) {
        return potSessionRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PotSessionDto> obtenirPotsParMembre(UUID membreId) {
        return potSessionRepository.findByMembreBeneficiaireId(membreId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PotSessionDto obtenirParId(UUID id) {
        PotSession entity = potSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tour de pot non trouve : " + id));
        return toDto(entity);
    }

    public PotSessionDto toDto(PotSession entity) {
        return new PotSessionDto(
                entity.getId(),
                entity.getGroupeId(),
                entity.getSessionTontineId(),
                entity.getReunionId(),
                entity.getMembreBeneficiaireId(),
                entity.getOrdrePassage(),
                entity.getMontantTotal(),
                entity.getMontantNet(),
                entity.getStatut(),
                entity.getModeVersement(),
                entity.getReferencePaiement(),
                entity.getDateAttribution(),
                entity.getDateVersement(),
                entity.getCreatedAt()
        );
    }
}
