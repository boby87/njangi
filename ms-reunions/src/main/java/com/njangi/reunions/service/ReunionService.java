package com.njangi.reunions.service;

import com.njangi.reunions.dto.CreateReunionRequest;
import com.njangi.reunions.dto.ReunionDto;
import com.njangi.reunions.entity.Reunion;
import com.njangi.reunions.entity.StatutReunion;
import com.njangi.reunions.event.ReunionEventPublisher;
import com.njangi.reunions.exception.BusinessException;
import com.njangi.reunions.exception.NotFoundException;
import com.njangi.reunions.repository.ReunionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ReunionService {

    private final ReunionRepository reunionRepository;
    private final ReunionEventPublisher eventPublisher;

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
        return reunionRepository.findByGroupeId(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ReunionDto create(CreateReunionRequest request) {
        Reunion reunion = Reunion.builder()
                .groupeId(request.groupeId())
                .sessionTontineId(request.sessionTontineId())
                .titre(request.titre())
                .dateReunion(request.dateReunion())
                .lieuReunion(request.lieuReunion())
                .typeSiege(request.typeSiege())
                .ordreJour(request.ordreJour())
                .presidentReunionId(request.presidentReunionId())
                .tresorierReunionId(request.tresorierReunionId())
                .statut(StatutReunion.PLANIFIEE)
                .build();

        Reunion saved = reunionRepository.save(reunion);
        log.info("Reunion creee : id={}, groupe={}", saved.getId(), saved.getGroupeId());
        return toDto(saved);
    }

    @Transactional
    public ReunionDto terminerReunion(UUID id, String compteRendu) {
        Reunion reunion = reunionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reunion", id));

        if (reunion.getStatut() == StatutReunion.TERMINEE) {
            throw new BusinessException("La reunion " + id + " est deja terminee");
        }
        if (reunion.getStatut() == StatutReunion.ANNULEE) {
            throw new BusinessException("Impossible de terminer une reunion annulee");
        }

        reunion.setStatut(StatutReunion.TERMINEE);
        reunion.setCompteRendu(compteRendu);
        Reunion updated = reunionRepository.save(reunion);

        eventPublisher.publierReunionTerminee(updated.getId(), updated.getGroupeId(), updated.getTitre());
        log.info("Reunion terminee : id={}", id);

        return toDto(updated);
    }

    private ReunionDto toDto(Reunion r) {
        return new ReunionDto(
                r.getId(),
                r.getGroupeId(),
                r.getSessionTontineId(),
                r.getTitre(),
                r.getDateReunion(),
                r.getLieuReunion(),
                r.getTypeSiege(),
                r.getStatut(),
                r.getOrdreJour(),
                r.getCompteRendu(),
                r.getPresidentReunionId(),
                r.getTresorierReunionId(),
                r.getCreatedAt(),
                r.getUpdatedAt()
        );
    }
}
