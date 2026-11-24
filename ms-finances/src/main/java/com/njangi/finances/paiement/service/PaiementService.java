package com.njangi.finances.paiement.service;

import com.njangi.finances.cotisation.service.CotisationService;
import com.njangi.finances.event.FinancesEventPublisher;
import com.njangi.finances.exception.BusinessException;
import com.njangi.finances.exception.NotFoundException;
import com.njangi.finances.paiement.dto.*;
import com.njangi.finances.paiement.entity.ModePaiement;
import com.njangi.finances.paiement.entity.Paiement;
import com.njangi.finances.paiement.entity.StatutPaiement;
import com.njangi.finances.paiement.repository.PaiementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PaiementService {

    private static final Logger log = LoggerFactory.getLogger(PaiementService.class);

    private final PaiementRepository paiementRepository;
    private final CotisationService cotisationService;
    private final FinancesEventPublisher eventPublisher;

    public PaiementService(PaiementRepository paiementRepository,
                           CotisationService cotisationService,
                           FinancesEventPublisher eventPublisher) {
        this.paiementRepository = paiementRepository;
        this.cotisationService = cotisationService;
        this.eventPublisher = eventPublisher;
    }

    public PaiementDto initierPaiementCash(InitierPaiementCashRequest request) {
        Optional<Paiement> existant = paiementRepository.findByCleIdempotence(request.cleIdempotence());
        if (existant.isPresent()) {
            log.info("Paiement Cash idempotent déjà existant pour cleIdempotence={}", request.cleIdempotence());
            return toDto(existant.get());
        }

        if (request.pieceJointeUrl() == null || request.pieceJointeUrl().isBlank()) {
            throw new BusinessException("La preuve de paiement (reçu physique signé / photo) est obligatoire pour tout règlement en espèces (Cash)");
        }

        String reference = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Paiement entity = new Paiement(
                null,
                request.cotisationId(),
                request.membreId(),
                request.groupeId(),
                request.montant(),
                ModePaiement.CASH,
                request.cleIdempotence(),
                reference,
                request.pieceJointeUrl(),
                null,
                "ESPECES",
                StatutPaiement.EN_ATTENTE_VALIDATION
        );
        entity.setCommentaire(request.commentaire());

        Paiement saved = paiementRepository.save(entity);
        eventPublisher.publierPaiementInitie(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());

        log.info("Paiement Cash initié avec pièce jointe : id={}, ref={}, montant={}",
                saved.getId(), saved.getReference(), saved.getMontant());
        return toDto(saved);
    }

    public PaiementDto validerPaiementCash(UUID paiementId, ValiderPaiementCashRequest request) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new NotFoundException("Paiement non trouvé : " + paiementId));

        if (paiement.getModePaiement() != ModePaiement.CASH) {
            throw new BusinessException("Seuls les règlements en espèces (Cash) sont soumis à la validation manuelle du trésorier");
        }

        if (paiement.getStatut() == StatutPaiement.VALIDE) {
            log.info("Paiement Cash {} déjà validé (opération idempotente)", paiementId);
            return toDto(paiement);
        }

        paiement.setValidePar(request.tresorierId());
        paiement.setDateValidation(LocalDateTime.now());
        if (request.commentaire() != null) {
            paiement.setCommentaire(request.commentaire());
        }

        if (request.valide()) {
            paiement.setStatut(StatutPaiement.VALIDE);
            Paiement saved = paiementRepository.save(paiement);

            // Mise à jour ACID de la cotisation associée dans la même transaction @Transactional
            if (saved.getCotisationId() != null) {
                cotisationService.appliquerPaiementSiExistante(saved.getCotisationId(), saved.getMontant());
            }

            eventPublisher.publierPaiementValide(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());
            log.info("Paiement Cash {} validé par le trésorier {}", paiementId, request.tresorierId());
            return toDto(saved);
        } else {
            paiement.setStatut(StatutPaiement.REJETE);
            Paiement saved = paiementRepository.save(paiement);
            eventPublisher.publierPaiementRejete(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getModePaiement(), StatutPaiement.REJETE.name(), request.commentaire());
            log.info("Paiement Cash {} rejeté par le trésorier {}", paiementId, request.tresorierId());
            return toDto(saved);
        }
    }

    public PaiementDto initierPaiementMobileMoney(InitierPaiementMobileMoneyRequest request) {
        Optional<Paiement> existant = paiementRepository.findByCleIdempotence(request.cleIdempotence());
        if (existant.isPresent()) {
            log.info("Paiement Mobile Money idempotent déjà existant pour cleIdempotence={}", request.cleIdempotence());
            return toDto(existant.get());
        }

        if (request.modePaiement() != ModePaiement.MTN_MOMO && request.modePaiement() != ModePaiement.ORANGE_MONEY) {
            throw new BusinessException("Mode de paiement Mobile Money non supporté : " + request.modePaiement());
        }

        String prefix = request.modePaiement() == ModePaiement.MTN_MOMO ? "MOMO-" : "OM-";
        String reference = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Paiement entity = new Paiement(
                null,
                request.cotisationId(),
                request.membreId(),
                request.groupeId(),
                request.montant(),
                request.modePaiement(),
                request.cleIdempotence(),
                reference,
                null,
                request.numeroTelephone(),
                request.modePaiement().name(),
                StatutPaiement.EN_COURS
        );

        Paiement saved = paiementRepository.save(entity);
        eventPublisher.publierPaiementInitie(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());

        log.info("Paiement Mobile Money {} initié : ref={}, téléphone={}",
                request.modePaiement(), saved.getReference(), saved.getNumeroTelephone());
        return toDto(saved);
    }

    public PaiementDto traiterWebhook(String operateur, WebhookPaiementRequest request) {
        Paiement paiement = paiementRepository.findByReference(request.reference())
                .or(() -> request.cleIdempotence() != null
                        ? paiementRepository.findByCleIdempotence(request.cleIdempotence())
                        : Optional.empty())
                .orElseThrow(() -> new NotFoundException("Paiement non trouvé pour la référence : " + request.reference()));

        if (paiement.getStatut() == StatutPaiement.VALIDE || paiement.getStatut() == StatutPaiement.ECHOUE) {
            log.info("Webhook {} idempotent reçu pour ref={} déjà au statut={}",
                    operateur, request.reference(), paiement.getStatut());
            return toDto(paiement);
        }

        paiement.setDateValidation(LocalDateTime.now());

        if (request.succes()) {
            paiement.setStatut(StatutPaiement.VALIDE);
            paiement.setCommentaire("Validé via webhook " + operateur + " (Ref: " + request.referenceExterneOperateur() + ")");
            Paiement saved = paiementRepository.save(paiement);

            // Mise à jour ACID de la cotisation associée dans la même transaction @Transactional
            if (saved.getCotisationId() != null) {
                cotisationService.appliquerPaiementSiExistante(saved.getCotisationId(), saved.getMontant());
            }

            eventPublisher.publierPaiementValide(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());
            log.info("Webhook {} confirme le succès du paiement ref={}", operateur, request.reference());
            return toDto(saved);
        } else {
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiement.setCommentaire("Échec via webhook " + operateur + " : " + request.message());
            Paiement saved = paiementRepository.save(paiement);
            eventPublisher.publierPaiementRejete(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getModePaiement(), StatutPaiement.ECHOUE.name(), request.message());
            log.warn("Webhook {} déclare l'échec du paiement ref={} : {}", operateur, request.reference(), request.message());
            return toDto(saved);
        }
    }

    @Transactional(readOnly = true)
    public PaiementDto obtenirParId(UUID id) {
        Paiement entity = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paiement non trouvé : " + id));
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> obtenirParCotisation(UUID cotisationId) {
        return paiementRepository.findByCotisationId(cotisationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> obtenirParGroupe(UUID groupeId) {
        return paiementRepository.findByGroupeId(groupeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> obtenirParMembre(UUID membreId) {
        return paiementRepository.findByMembreId(membreId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PaiementDto toDto(Paiement p) {
        return new PaiementDto(
                p.getId(),
                p.getCotisationId(),
                p.getMembreId(),
                p.getGroupeId(),
                p.getMontant(),
                p.getModePaiement(),
                p.getCleIdempotence(),
                p.getReference(),
                p.getPieceJointeUrl(),
                p.getNumeroTelephone(),
                p.getOperateur(),
                p.getStatut(),
                p.getValidePar(),
                p.getDateValidation(),
                p.getCommentaire(),
                p.getDatePaiement(),
                p.getCreatedAt()
        );
    }
}
