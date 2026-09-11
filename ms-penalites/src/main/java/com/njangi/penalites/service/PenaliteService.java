package com.njangi.penalites.service;

import com.njangi.penalites.dto.PenaliteDto;
import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.Penalite.StatutPenalite;
import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.exception.PenaliteNotFoundException;
import com.njangi.penalites.kafka.PenaliteEventPublisher;
import com.njangi.penalites.repository.PenaliteRepository;
import com.njangi.penalites.repository.TarificationPenaliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PenaliteService {

    private final PenaliteRepository penaliteRepository;
    private final TarificationPenaliteRepository tarificationRepository;
    private final PenaliteEventPublisher eventPublisher;

    /**
     * Applique une pénalité à un membre.
     * Le montant est automatiquement résolu depuis la tarification active du groupe.
     * Si aucune tarification n'existe, le montant du DTO est utilisé (doit être non nul).
     */
    public PenaliteDto appliquer(PenaliteDto dto) {
        log.info("Application d'une pénalité type={} pour membre={} dans groupe={}",
                dto.typeInfraction(), dto.membreId(), dto.groupeId());

        // Résolution du montant via la tarification active du groupe
        var montant = tarificationRepository
                .findByGroupeIdAndTypeInfractionAndActifTrue(dto.groupeId(), dto.typeInfraction())
                .map(TarificationPenalite::getMontant)
                .orElse(dto.montant());

        if (montant == null) {
            throw new IllegalArgumentException(
                "Aucune tarification active trouvée pour le type " + dto.typeInfraction()
                + " dans le groupe " + dto.groupeId() + " et aucun montant fourni");
        }

        Penalite penalite = Penalite.builder()
                .membreId(dto.membreId())
                .groupeId(dto.groupeId())
                .sessionId(dto.sessionId())
                .typeInfraction(dto.typeInfraction())
                .montant(montant)
                .statut(StatutPenalite.EN_ATTENTE)
                .build();

        Penalite saved = penaliteRepository.save(penalite);
        eventPublisher.publishPenaliteAppliquee(saved);
        log.info("Pénalité appliquée avec id={}", saved.getId());
        return PenaliteDto.from(saved);
    }

    public PenaliteDto payer(UUID id) {
        log.info("Paiement de la pénalité id={}", id);
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new PenaliteNotFoundException(id));
        if (penalite.getStatut() != StatutPenalite.EN_ATTENTE) {
            throw new IllegalStateException(
                "La pénalité id=" + id + " ne peut pas être payée (statut: " + penalite.getStatut() + ")");
        }
        penalite.setStatut(StatutPenalite.PAYEE);
        return PenaliteDto.from(penaliteRepository.save(penalite));
    }

    public PenaliteDto annuler(UUID id) {
        log.info("Annulation de la pénalité id={}", id);
        Penalite penalite = penaliteRepository.findById(id)
                .orElseThrow(() -> new PenaliteNotFoundException(id));
        penalite.setStatut(StatutPenalite.ANNULEE);
        return PenaliteDto.from(penaliteRepository.save(penalite));
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> findByMembre(UUID membreId) {
        return penaliteRepository.findByMembreId(membreId)
                .stream().map(PenaliteDto::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PenaliteDto> findByGroupe(UUID groupeId) {
        return penaliteRepository.findByGroupeId(groupeId)
                .stream().map(PenaliteDto::from).toList();
    }

    @Transactional(readOnly = true)
    public PenaliteDto findById(UUID id) {
        return penaliteRepository.findById(id)
                .map(PenaliteDto::from)
                .orElseThrow(() -> new PenaliteNotFoundException(id));
    }
}
