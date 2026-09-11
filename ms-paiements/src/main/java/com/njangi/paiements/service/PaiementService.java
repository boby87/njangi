package com.njangi.paiements.service;

import com.njangi.paiements.dto.CreatePaiementRequest;
import com.njangi.paiements.dto.PaiementDto;
import com.njangi.paiements.dto.ValiderPaiementRequest;
import com.njangi.paiements.entity.Paiement;
import com.njangi.paiements.entity.StatutPaiement;
import com.njangi.paiements.event.PaiementEventPublisher;
import com.njangi.paiements.exception.BusinessException;
import com.njangi.paiements.exception.NotFoundException;
import com.njangi.paiements.repository.PaiementRepository;
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
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final PaiementEventPublisher eventPublisher;

    public List<PaiementDto> findAll() {
        return paiementRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public PaiementDto findById(UUID id) {
        return paiementRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Paiement", id));
    }

    public List<PaiementDto> findByMembre(UUID membreId) {
        return paiementRepository.findByMembreId(membreId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<PaiementDto> findByGroupe(UUID groupeId) {
        return paiementRepository.findByGroupeId(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PaiementDto create(CreatePaiementRequest request) {
        // Genere une reference unique si non fournie
        String reference = (request.reference() != null && !request.reference().isBlank())
                ? request.reference()
                : "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Verifie l'unicite de la reference
        if (paiementRepository.findByReference(reference).isPresent()) {
            throw new BusinessException("Un paiement avec la reference " + reference + " existe deja");
        }

        Paiement paiement = Paiement.builder()
                .cotisationId(request.cotisationId())
                .membreId(request.membreId())
                .groupeId(request.groupeId())
                .montant(request.montant())
                .modePaiement(request.modePaiement())
                .reference(reference)
                .commentaire(request.commentaire())
                .statut(StatutPaiement.EN_ATTENTE)
                .otpVerifie(false)
                .build();

        Paiement saved = paiementRepository.save(paiement);
        log.info("Paiement cree : id={}, reference={}, membre={}", saved.getId(), saved.getReference(), saved.getMembreId());
        return toDto(saved);
    }

    @Transactional
    public PaiementDto validerPaiement(UUID id, ValiderPaiementRequest request) {
        Paiement paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paiement", id));

        if (paiement.getStatut() == StatutPaiement.VALIDE) {
            throw new BusinessException("Le paiement " + id + " est deja valide");
        }
        if (paiement.getStatut() == StatutPaiement.REJETE) {
            throw new BusinessException("Impossible de valider un paiement rejete");
        }

        // Verification OTP simulee — en production, deleguer au ms-auth
        boolean otpValide = verifierOtp(request.otpCode(), paiement.getMembreId());
        if (!otpValide) {
            throw new BusinessException("Code OTP invalide ou expire");
        }

        paiement.setStatut(StatutPaiement.VALIDE);
        paiement.setValidePar(request.validePar());
        paiement.setDateValidation(LocalDateTime.now());
        paiement.setOtpVerifie(true);
        if (request.commentaire() != null) {
            paiement.setCommentaire(request.commentaire());
        }

        Paiement updated = paiementRepository.save(paiement);

        eventPublisher.publierPaiementValide(
                updated.getId(), updated.getCotisationId(),
                updated.getMembreId(), updated.getGroupeId(), updated.getMontant());

        log.info("Paiement valide : id={}, validePar={}", id, request.validePar());
        return toDto(updated);
    }

    /**
     * Verification OTP simulee.
     * En production : appel HTTP vers ms-auth pour verifier le code OTP.
     */
    private boolean verifierOtp(String otpCode, UUID membreId) {
        // Simulation : tout OTP de 6 chiffres est accepte
        return otpCode != null && otpCode.matches("\\d{6}");
    }

    private PaiementDto toDto(Paiement p) {
        return new PaiementDto(
                p.getId(), p.getCotisationId(), p.getMembreId(), p.getGroupeId(),
                p.getMontant(), p.getModePaiement(), p.getReference(), p.getStatut(),
                p.getDatePaiement(), p.getValidePar(), p.getDateValidation(),
                p.getCommentaire(), p.getOtpVerifie(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
