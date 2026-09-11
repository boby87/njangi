package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.*;
import com.njangi.cotisations.entity.Cotisation;
import com.njangi.cotisations.entity.PotSession;
import com.njangi.cotisations.entity.StatutCotisation;
import com.njangi.cotisations.entity.StatutPot;
import com.njangi.cotisations.event.CotisationEventPublisher;
import com.njangi.cotisations.exception.BusinessException;
import com.njangi.cotisations.exception.NotFoundException;
import com.njangi.cotisations.repository.CotisationRepository;
import com.njangi.cotisations.repository.PotSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class CotisationService {

    private final CotisationRepository cotisationRepository;
    private final PotSessionRepository potSessionRepository;
    private final CotisationEventPublisher eventPublisher;

    public List<CotisationDto> findAll() {
        return cotisationRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public CotisationDto findById(UUID id) {
        return cotisationRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Cotisation", id));
    }

    public List<CotisationDto> findByMembre(UUID membreId) {
        return cotisationRepository.findByMembreId(membreId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<CotisationDto> findByGroupe(UUID groupeId) {
        return cotisationRepository.findByGroupeId(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CotisationDto create(CreateCotisationRequest request) {
        Cotisation cotisation = Cotisation.builder()
                .groupeId(request.groupeId())
                .membreId(request.membreId())
                .typeCotisationId(request.typeCotisationId())
                .reunionId(request.reunionId())
                .montantDu(request.montantDu())
                .dateLimitePaiement(request.dateLimitePaiement())
                .statut(StatutCotisation.EN_ATTENTE)
                .build();

        Cotisation saved = cotisationRepository.save(cotisation);
        log.info("Cotisation creee : id={}, membre={}", saved.getId(), saved.getMembreId());
        return toDto(saved);
    }

    @Transactional
    public CotisationDto marquerPayee(UUID cotisationId) {
        Cotisation cotisation = cotisationRepository.findById(cotisationId)
                .orElseThrow(() -> new NotFoundException("Cotisation", cotisationId));

        if (cotisation.getStatut() == StatutCotisation.PAYEE) {
            throw new BusinessException("La cotisation " + cotisationId + " est deja payee");
        }

        cotisation.setMontantPaye(cotisation.getMontantDu());
        cotisation.setStatut(StatutCotisation.PAYEE);
        Cotisation updated = cotisationRepository.save(cotisation);

        eventPublisher.publierCotisationPayee(
                updated.getId(), updated.getMembreId(), updated.getGroupeId(), updated.getMontantPaye());
        log.info("Cotisation marquee payee : id={}", cotisationId);

        return toDto(updated);
    }

    @Transactional
    public PotSessionDto verserPot(VerserPotRequest request) {
        PotSession pot = PotSession.builder()
                .groupeId(request.groupeId())
                .sessionTontineId(request.sessionTontineId())
                .reunionId(request.reunionId())
                .membreBeneficiaireId(request.membreBeneficiaireId())
                .montantTotal(request.montantTotal())
                .statut(StatutPot.VERSE)
                .dateVersement(LocalDateTime.now())
                .build();

        PotSession saved = potSessionRepository.save(pot);
        eventPublisher.publierPotVerse(
                saved.getId(), saved.getGroupeId(), saved.getMembreBeneficiaireId(), saved.getMontantTotal());
        log.info("Pot verse : id={}, beneficiaire={}, montant={}", saved.getId(), saved.getMembreBeneficiaireId(), saved.getMontantTotal());

        return toPotDto(saved);
    }

    private CotisationDto toDto(Cotisation c) {
        return new CotisationDto(
                c.getId(), c.getGroupeId(), c.getMembreId(), c.getTypeCotisationId(),
                c.getReunionId(), c.getMontantDu(), c.getMontantPaye(), c.getStatut(),
                c.getDateLimitePaiement(), c.getCreatedAt(), c.getUpdatedAt()
        );
    }

    private PotSessionDto toPotDto(PotSession p) {
        return new PotSessionDto(
                p.getId(), p.getGroupeId(), p.getSessionTontineId(), p.getReunionId(),
                p.getMembreBeneficiaireId(), p.getMontantTotal(), p.getStatut(),
                p.getDateVersement(), p.getCreatedAt()
        );
    }
}
