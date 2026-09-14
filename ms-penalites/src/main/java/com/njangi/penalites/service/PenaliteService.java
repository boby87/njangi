package com.njangi.penalites.service;

import com.njangi.penalites.dto.InfligerPenaliteRequest;
import com.njangi.penalites.dto.PenaliteDto;
import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.StatutPenalite;
import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.event.PenaliteEventPublisher;
import com.njangi.penalites.exception.BusinessException;
import com.njangi.penalites.exception.NotFoundException;
import com.njangi.penalites.repository.PenaliteRepository;
import com.njangi.penalites.repository.TarificationPenaliteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PenaliteService {

    private static final Logger log = LoggerFactory.getLogger(PenaliteService.class);

    private final PenaliteRepository penaliteRepository;
    private final TarificationPenaliteRepository tarificationRepository;
    private final PenaliteEventPublisher eventPublisher;

    public PenaliteService(PenaliteRepository penaliteRepository,
                           TarificationPenaliteRepository tarificationRepository,
                           PenaliteEventPublisher eventPublisher) {
        this.penaliteRepository = penaliteRepository;
        this.tarificationRepository = tarificationRepository;
        this.eventPublisher = eventPublisher;
    }

    public PenaliteDto infligerPenalite(InfligerPenaliteRequest request) {
        // Résolution dynamique du montant via le barème actif du groupe
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
        eventPublisher.publierPenaliteInfligee(saved);

        log.info("Penalite infligee : id={}, membre={}, type={}, montant={}",
                saved.getId(), saved.getMembreId(), saved.getTypeInfraction(), saved.getMontant());
        return toDto(saved);
    }

    public PenaliteDto payer(UUID id) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penalite non trouvee : " + id));

        if (penalite.getStatut() != StatutPenalite.EN_ATTENTE) {
            throw new BusinessException("La penalite " + id + " ne peut pas etre payee (statut actuel: " + penalite.getStatut() + ")");
        }

        penalite.setStatut(StatutPenalite.PAYEE);
        penalite.setDatePaiement(LocalDateTime.now());
        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierPenalitePayee(saved);

        log.info("Penalite reglee : id={}, montant={}", saved.getId(), saved.getMontant());
        return toDto(saved);
    }

    public PenaliteDto annuler(UUID id, String motif) {
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penalite non trouvee : " + id));

        if (penalite.getStatut() == StatutPenalite.PAYEE) {
            throw new BusinessException("Une penalite deja payee ne peut pas etre annulee");
        }

        penalite.setStatut(StatutPenalite.ANNULEE);
        if (motif != null && !motif.isBlank()) {
            penalite.setMotif(motif);
        }
        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publierPenaliteAnnulee(saved);

        log.info("Penalite annulee : id={}", saved.getId());
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public PenaliteDto obtenirParId(UUID id) {
        Penalite entity = penaliteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Penalite non trouvee : " + id));
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
