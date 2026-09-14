package com.njangi.reunions.service;

import com.njangi.reunions.dto.BatchPresencesRequest;
import com.njangi.reunions.dto.EnregistrerPresenceRequest;
import com.njangi.reunions.dto.PresenceDto;
import com.njangi.reunions.dto.QuorumDto;
import com.njangi.reunions.entity.Presence;
import com.njangi.reunions.entity.StatutPresence;
import com.njangi.reunions.event.ReunionEventPublisher;
import com.njangi.reunions.exception.NotFoundException;
import com.njangi.reunions.repository.PresenceRepository;
import com.njangi.reunions.repository.ReunionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PresenceService {

    private static final Logger log = LoggerFactory.getLogger(PresenceService.class);

    private final PresenceRepository presenceRepository;
    private final ReunionRepository reunionRepository;
    private final ReunionEventPublisher eventPublisher;

    public PresenceService(PresenceRepository presenceRepository,
                           ReunionRepository reunionRepository,
                           ReunionEventPublisher eventPublisher) {
        this.presenceRepository = presenceRepository;
        this.reunionRepository = reunionRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<PresenceDto> getPresences(UUID reunionId) {
        verifierExistenceReunion(reunionId);
        return presenceRepository.findByReunionId(reunionId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PresenceDto enregistrerPresence(UUID reunionId, EnregistrerPresenceRequest req) {
        verifierExistenceReunion(reunionId);

        Presence presence = presenceRepository.findByReunionIdAndMembreId(reunionId, req.membreId())
                .orElseGet(() -> {
                    Presence p = new Presence();
                    p.setReunionId(reunionId);
                    p.setMembreId(req.membreId());
                    p.setEnregistreLe(LocalDateTime.now());
                    return p;
                });

        presence.setStatut(req.statut());
        presence.setHeureArrivee(req.heureArrivee() != null ? req.heureArrivee() : LocalDateTime.now());
        presence.setJustification(req.justification());
        presence.setProcuration(req.procuration() != null ? req.procuration() : false);
        presence.setMandataireId(req.mandataireId());

        Presence saved = presenceRepository.save(presence);
        log.info("Presence enregistree : reunionId={}, membreId={}, statut={}",
                reunionId, req.membreId(), req.statut());

        eventPublisher.publierPresenceEnregistree(reunionId, req.membreId(), req.statut().name());

        return toDto(saved);
    }

    @Transactional
    public List<PresenceDto> enregistrerPresencesBatch(UUID reunionId, BatchPresencesRequest req) {
        verifierExistenceReunion(reunionId);

        List<PresenceDto> results = new ArrayList<>();
        for (EnregistrerPresenceRequest item : req.presences()) {
            results.add(enregistrerPresence(reunionId, item));
        }
        return results;
    }

    public QuorumDto calculerQuorum(UUID reunionId) {
        verifierExistenceReunion(reunionId);

        long total = presenceRepository.countByReunionId(reunionId);
        long presents = presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.PRESENT);
        long retards = presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.RETARD);
        long excuses = presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.EXCUSE);
        long absents = presenceRepository.countByReunionIdAndStatut(reunionId, StatutPresence.ABSENT);

        long effectifsPresents = presents + retards;
        double taux = total > 0 ? ((double) effectifsPresents / total) * 100.0 : 0.0;
        // Quorum statutaire standard tontine : majorité simple (>= 50%)
        boolean quorumAtteint = total > 0 && taux >= 50.0;

        return new QuorumDto(
                reunionId,
                total,
                presents,
                retards,
                excuses,
                absents,
                Math.round(taux * 100.0) / 100.0,
                quorumAtteint
        );
    }

    private void verifierExistenceReunion(UUID reunionId) {
        if (!reunionRepository.existsById(reunionId)) {
            throw new NotFoundException("Reunion", reunionId);
        }
    }

    private PresenceDto toDto(Presence p) {
        return new PresenceDto(
                p.getId(),
                p.getReunionId(),
                p.getMembreId(),
                p.getStatut(),
                p.getHeureArrivee(),
                p.getJustification(),
                p.getProcuration(),
                p.getMandataireId(),
                p.getEnregistreLe()
        );
    }
}
