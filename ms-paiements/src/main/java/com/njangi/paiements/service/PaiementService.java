package com.njangi.paiements.service;

import com.njangi.paiements.dto.*;
import com.njangi.paiements.entity.ModePaiement;
import com.njangi.paiements.entity.Paiement;
import com.njangi.paiements.entity.StatutPaiement;
import com.njangi.paiements.event.PaiementEventPublisher;
import com.njangi.paiements.exception.BusinessException;
import com.njangi.paiements.exception.NotFoundException;
import com.njangi.paiements.repository.PaiementRepository;
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
    private final PaiementEventPublisher eventPublisher;

    public PaiementService(PaiementRepository paiementRepository,
                           PaiementEventPublisher eventPublisher) {
        this.paiementRepository = paiementRepository;
        this.eventPublisher = eventPublisher;
    }

    public PaiementDto initierPaiementCash(InitierPaiementCashRequest request) {
        // Contrôle d'idempotence strict
        Optional<Paiement> existant = paiementRepository.findByCleIdempotence(request.cleIdempotence());
        if (existant.isPresent()) {
            log.info("Paiement Cash idempotent deja existant pour cleIdempotence={}", request.cleIdempotence());
            return toDto(existant.get());
        }

        // Règle GEMINI.md obligatoire : tout paiement en espèces requiert impérativement un reçu physique signé
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

        log.info("Paiement Cash initie avec piece jointe : id={}, ref={}, montant={}",
                saved.getId(), saved.getReference(), saved.getMontant());
        return toDto(saved);
    }

    public PaiementDto validerPaiementCash(UUID paiementId, ValiderPaiementCashRequest request) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new NotFoundException("Paiement non trouve : " + paiementId));

        if (paiement.getModePaiement() != ModePaiement.CASH) {
            throw new BusinessException("Seuls les règlements en espèces (Cash) sont soumis à la validation manuelle du trésorier");
        }

        if (paiement.getStatut() == StatutPaiement.VALIDE) {
            log.info("Paiement Cash {} deja valide (operation idempotente)", paiementId);
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
            eventPublisher.publierPaiementValide(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());
            log.info("Paiement Cash {} valide par le tresorier {}", paiementId, request.tresorierId());
            return toDto(saved);
        } else {
            paiement.setStatut(StatutPaiement.REJETE);
            Paiement saved = paiementRepository.save(paiement);
            eventPublisher.publierPaiementRejete(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    request.commentaire());
            log.info("Paiement Cash {} rejete par le tresorier {}", paiementId, request.tresorierId());
            return toDto(saved);
        }
    }

    public PaiementDto initierPaiementMobileMoney(InitierPaiementMobileMoneyRequest request) {
        // Contrôle d'idempotence
        Optional<Paiement> existant = paiementRepository.findByCleIdempotence(request.cleIdempotence());
        if (existant.isPresent()) {
            log.info("Paiement Mobile Money idempotent deja existant pour cleIdempotence={}", request.cleIdempotence());
            return toDto(existant.get());
        }

        if (request.modePaiement() != ModePaiement.MTN_MOMO && request.modePaiement() != ModePaiement.ORANGE_MONEY) {
            throw new BusinessException("Mode de paiement Mobile Money non supporte : " + request.modePaiement());
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

        log.info("Paiement Mobile Money {} initie : ref={}, telephone={}",
                request.modePaiement(), saved.getReference(), saved.getNumeroTelephone());
        return toDto(saved);
    }

    public PaiementDto traiterWebhook(String operateur, WebhookPaiementRequest request) {
        Paiement paiement = paiementRepository.findByReference(request.reference())
                .or(() -> request.cleIdempotence() != null
                        ? paiementRepository.findByCleIdempotence(request.cleIdempotence())
                        : Optional.empty())
                .orElseThrow(() -> new NotFoundException("Paiement non trouve pour la reference : " + request.reference()));

        // Idempotence : si le paiement est déjà dans un état terminal, ne rien ré-exécuter
        if (paiement.getStatut() == StatutPaiement.VALIDE || paiement.getStatut() == StatutPaiement.ECHOUE) {
            log.info("Webhook {} idempotent recu pour ref={} deja au statut={}",
                    operateur, request.reference(), paiement.getStatut());
            return toDto(paiement);
        }

        paiement.setDateValidation(LocalDateTime.now());

        if (request.succes()) {
            paiement.setStatut(StatutPaiement.VALIDE);
            paiement.setCommentaire("Valide via webhook " + operateur + " (Ref: " + request.referenceExterneOperateur() + ")");
            Paiement saved = paiementRepository.save(paiement);
            eventPublisher.publierPaiementValide(saved.getId(), saved.getCotisationId(), saved.getMembreId(),
                    saved.getGroupeId(), saved.getMontant(), saved.getModePaiement(), saved.getReference());
            log.info("Webhook {} confirme le succes du paiement ref={}", operateur, request.reference());
            return toDto(saved);
        } else {
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiement.setCommentaire("Echec via webhook " + operateur + " : " + request.message());
            Paiement saved = paiementRepository.save(paiement);
            eventPublisher.publierPaiementEchoue(saved.getId(), saved.getCotisationId(), saved.getMembreId(), request.message());
            log.warn("Webhook {} declare l'echec du paiement ref={} : {}", operateur, request.reference(), request.message());
            return toDto(saved);
        }
    }

    @Transactional(readOnly = true)
    public PaiementDto obtenirParId(UUID id) {
        Paiement entity = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paiement non trouve : " + id));
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
