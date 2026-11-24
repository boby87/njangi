package com.njangi.finances.penalite.service;

import com.njangi.finances.event.FinancesEventPublisher;
import com.njangi.finances.exception.BusinessException;
import com.njangi.finances.exception.NotFoundException;
import com.njangi.finances.penalite.dto.InfligerPenaliteRequest;
import com.njangi.finances.penalite.dto.PenaliteDto;
import com.njangi.finances.penalite.entity.Penalite;
import com.njangi.finances.penalite.entity.StatutPenalite;
import com.njangi.finances.penalite.entity.TarificationPenalite;
import com.njangi.finances.penalite.entity.TypeInfraction;
import com.njangi.finances.penalite.repository.PenaliteRepository;
import com.njangi.finances.penalite.repository.TarificationPenaliteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PenaliteService {

    private static final Logger log = LoggerFactory.getLogger(PenaliteService.class);

    private final PenaliteRepository penaliteRepository;
    private final TarificationPenaliteRepository tarificationRepository;
    private final FinancesEventPublisher eventPublisher;

    public PenaliteService(PenaliteRepository penaliteRepository,
                           TarificationPenaliteRepository tarificationRepository,
                           FinancesEventPublisher eventPublisher) {
        this.penaliteRepository = penaliteRepository;
        this.tarificationRepository = tarificationRepository;
        this.eventPublisher = eventPublisher;
    }

    public PenaliteDto infligerPenalite(InfligerPenaliteRequest request) {
        BigDecimal montant = request.montant();
        if (montant == null) {
            montant = tarificationRepository
                    .findByGroupeIdAndTypeInfractionAndActifTrue(request.groupeId(), request.typeInfraction())
                    .map(TarificationPenalite::getMontant)
                    .orElseThrow(() -> new BusinessException(
                            "Aucun tarif actif n'est configuré pour l'infraction " + request.typeInfraction()
                                    + " dans le groupe " + request.groupeId()));
        }

        Penalite penalite = new Penalite(
                null,
                request.membreId(),
                request.groupeId(),
                request.sessionId(),
                request.reunionId(),
                request.typeInfraction(),
                montant,
                StatutPenalite.EN_ATTENTE,
                request.motif()
        );

        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierSanctionInfligee(saved);

        log.info("Pénalité infligée : id={}, membre={}, type={}, montant={}",
                saved.getId(), saved.getMembreId(), saved.getTypeInfraction(), saved.getMontant());
        return toDto(saved);
    }

    /**
     * Applique automatiquement une pénalité RETARD_PAIEMENT dans la même transaction ACID
     * si un barème actif est configuré pour le groupe.
     */
    public Optional<PenaliteDto> appliquerPenaliteRetardSiTarifConfigure(
            UUID membreId, UUID groupeId, UUID sessionId, UUID reunionId, UUID cotisationId) {
        Optional<TarificationPenalite> tarifOpt = tarificationRepository
                .findByGroupeIdAndTypeInfractionAndActifTrue(groupeId, TypeInfraction.RETARD_PAIEMENT);

        if (tarifOpt.isEmpty()) {
            log.debug("Aucun tarif actif RETARD_PAIEMENT configuré pour le groupe {} (cotisation {})", groupeId, cotisationId);
            return Optional.empty();
        }

        UUID resolvedSessionId = sessionId != null ? sessionId : (reunionId != null ? reunionId : UUID.randomUUID());
        Penalite penalite = new Penalite(
                null,
                membreId,
                groupeId,
                resolvedSessionId,
                reunionId,
                TypeInfraction.RETARD_PAIEMENT,
                tarifOpt.get().getMontant(),
                StatutPenalite.EN_ATTENTE,
                "Pénalité automatique suite au retard de paiement de la cotisation " + cotisationId
        );

        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierSanctionInfligee(saved);

        log.info("Pénalité automatique RETARD_PAIEMENT appliquée dans la transaction : penaliteId={}, cotisationId={}, montant={}",
                saved.getId(), cotisationId, saved.getMontant());
        return Optional.of(toDto(saved));
    }

    public PenaliteDto payer(UUID id) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pénalité non trouvée : " + id));

        if (penalite.getStatut() != StatutPenalite.EN_ATTENTE) {
            throw new BusinessException("La penalite " + id + " ne peut pas etre payee (statut actuel: " + penalite.getStatut() + ")");
        }

        penalite.setStatut(StatutPenalite.PAYEE);
        penalite.setDatePaiement(LocalDateTime.now());
        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierSanctionPayee(saved);

        log.info("Pénalité réglée : id={}, montant={}", saved.getId(), saved.getMontant());
        return toDto(saved);
    }

    public PenaliteDto annuler(UUID id, String motif) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pénalité non trouvée : " + id));

        if (penalite.getStatut() == StatutPenalite.PAYEE) {
            throw new BusinessException("Une pénalité déjà payée ne peut pas être annulée");
        }

        penalite.setStatut(StatutPenalite.ANNULEE);
        if (motif != null && !motif.isBlank()) {
            penalite.setMotif(motif);
        }
        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierSanctionAnnulee(saved);

        log.info("Pénalité annulée : id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PenaliteDto obtenirParId(UUID id) {
        Penalite entity = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pénalité non trouvée : " + id));
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> obtenirParMembre(UUID membreId) {
        return penaliteRepository.findByMembreId(membreId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> obtenirParGroupe(UUID groupeId) {
        return penaliteRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> obtenirParReunion(UUID reunionId) {
        return penaliteRepository.findByReunionId(reunionId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> obtenirParSession(UUID sessionId) {
        return penaliteRepository.findBySessionId(sessionId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PenaliteDto toDto(Penalite p) {
        return PenaliteDto.from(p);
    }
}
