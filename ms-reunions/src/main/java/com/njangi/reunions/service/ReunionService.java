package com.njangi.reunions.service;

import com.njangi.reunions.dto.CloturerReunionRequest;
import com.njangi.reunions.dto.CreateReunionRequest;
import com.njangi.reunions.dto.ReunionDto;
import com.njangi.reunions.dto.UpdateReunionRequest;
import com.njangi.reunions.entity.Reunion;
import com.njangi.reunions.entity.StatutReunion;
import com.njangi.reunions.entity.TypeSiege;
import com.njangi.reunions.event.ReunionEventPublisher;
import com.njangi.reunions.exception.BusinessException;
import com.njangi.reunions.exception.NotFoundException;
import com.njangi.reunions.repository.ReunionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ReunionService {

    private static final Logger log = LoggerFactory.getLogger(ReunionService.class);

    private final ReunionRepository reunionRepository;
    private final ReunionEventPublisher eventPublisher;

    public ReunionService(ReunionRepository reunionRepository,
                          ReunionEventPublisher eventPublisher) {
        this.reunionRepository = reunionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<ReunionDto> findAll() {
        return reunionRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public ReunionDto findById(UUID id) {
        return reunionRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Reunion", id));
    }

    public List<ReunionDto> findByGroupe(UUID groupeId) {
        return reunionRepository.findByGroupeIdOrderByDateReunionDesc(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    public List<ReunionDto> findByGroupeAndStatut(UUID groupeId, StatutReunion statut) {
        return reunionRepository.findByGroupeIdAndStatut(groupeId, statut).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ReunionDto create(CreateReunionRequest request) {
        Reunion reunion = new Reunion();
        reunion.setGroupeId(request.groupeId());
        reunion.setSessionTontineId(request.sessionTontineId());
        reunion.setTitre(request.titre());
        reunion.setDateReunion(request.dateReunion());
        reunion.setLieuReunion(request.lieuReunion());
        reunion.setTypeSiege(request.typeSiege() != null ? request.typeSiege() : TypeSiege.FIXE);
        reunion.setHoteId(request.hoteId());
        reunion.setOrdreJour(request.ordreJour());
        reunion.setPresidentReunionId(request.presidentReunionId());
        reunion.setSecretaireReunionId(request.secretaireReunionId());
        reunion.setTresorierReunionId(request.tresorierReunionId());
        reunion.setStatut(StatutReunion.PLANIFIEE);

        Reunion saved = reunionRepository.save(reunion);
        log.info("Reunion planifiee avec succes : id={}, groupe={}, titre='{}'",
                saved.getId(), saved.getGroupeId(), saved.getTitre());

        eventPublisher.publierReunionCreee(saved.getId(), saved.getGroupeId(), saved.getTitre(), saved.getDateReunion());

        return toDto(saved);
    }

    @Transactional
    public ReunionDto update(UUID id, UpdateReunionRequest request) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));

        if (reunion.getStatut() == StatutReunion.TERMINEE || reunion.getStatut() == StatutReunion.ANNULEE) {
            throw new BusinessException("Impossible de modifier une reunion terminee ou annulee");
        }

        reunion.setTitre(request.titre());
        reunion.setDateReunion(request.dateReunion());
        reunion.setLieuReunion(request.lieuReunion());
        if (request.typeSiege() != null) {
            reunion.setTypeSiege(request.typeSiege());
        }
        reunion.setHoteId(request.hoteId());
        reunion.setOrdreJour(request.ordreJour());
        reunion.setPresidentReunionId(request.presidentReunionId());
        reunion.setSecretaireReunionId(request.secretaireReunionId());
        reunion.setTresorierReunionId(request.tresorierReunionId());

        Reunion updated = reunionRepository.save(reunion);
        log.info("Reunion mise a jour : id={}", id);
        return toDto(updated);
    }

    @Transactional
    public ReunionDto demarrerReunion(UUID id) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));

        if (reunion.getStatut() == StatutReunion.TERMINEE) {
            throw new BusinessException("La reunion est deja terminee");
        }
        if (reunion.getStatut() == StatutReunion.ANNULEE) {
            throw new BusinessException("Impossible de demarrer une reunion annulee");
        }

        reunion.setStatut(StatutReunion.EN_COURS);
        Reunion saved = reunionRepository.save(reunion);
        log.info("Reunion demarree en direct : id={}", id);

        eventPublisher.publierReunionDemarree(saved.getId(), saved.getGroupeId(), saved.getTitre());

        return toDto(saved);
    }

    @Transactional
    public ReunionDto terminerReunion(UUID id, CloturerReunionRequest request) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));

        if (reunion.getStatut() == StatutReunion.TERMINEE) {
            throw new BusinessException("La reunion " + id + " est deja terminee");
        }
        if (reunion.getStatut() == StatutReunion.ANNULEE) {
            throw new BusinessException("Impossible de terminer une reunion annulee");
        }

        reunion.setStatut(StatutReunion.TERMINEE);
        if (request != null && request.compteRendu() != null) {
            reunion.setCompteRendu(request.compteRendu());
        }
        Reunion updated = reunionRepository.save(reunion);

        eventPublisher.publierReunionTerminee(updated.getId(), updated.getGroupeId(), updated.getTitre(), updated.getCompteRendu());
        log.info("Reunion terminee avec succes : id={}", id);

        return toDto(updated);
    }

    @Transactional
    public ReunionDto annulerReunion(UUID id) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));

        if (reunion.getStatut() == StatutReunion.TERMINEE) {
            throw new BusinessException("Impossible d'annuler une reunion deja terminee");
        }

        reunion.setStatut(StatutReunion.ANNULEE);
        Reunion updated = reunionRepository.save(reunion);
        log.info("Reunion annulee : id={}", id);
        return toDto(updated);
    }

    @Transactional
    public ReunionDto updateOrdreJour(UUID id, String ordreJour) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));
        reunion.setOrdreJour(ordreJour);
        Reunion updated = reunionRepository.save(reunion);
        return toDto(updated);
    }

    @Transactional
    public ReunionDto updateCompteRendu(UUID id, String compteRendu) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));
        reunion.setCompteRendu(compteRendu);
        Reunion updated = reunionRepository.save(reunion);
        return toDto(updated);
    }

    public ReunionDto toDto(Reunion r) {
        return new ReunionDto(
                r.getId(),
                r.getGroupeId(),
                r.getSessionTontineId(),
                r.getTitre(),
                r.getDateReunion(),
                r.getLieuReunion(),
                r.getTypeSiege(),
                r.getHoteId(),
                r.getStatut(),
                r.getOrdreJour(),
                r.getCompteRendu(),
                r.getPresidentReunionId(),
                r.getSecretaireReunionId(),
                r.getTresorierReunionId(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
