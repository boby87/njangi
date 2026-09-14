package com.njangi.groupes.service;

import com.njangi.groupes.dto.CreerSessionRequest;
import com.njangi.groupes.dto.SessionTontineDto;
import com.njangi.groupes.entity.SessionTontine;
import com.njangi.groupes.entity.StatutSession;
import com.njangi.groupes.event.GroupeEventPublisher;
import com.njangi.groupes.exception.BusinessException;
import com.njangi.groupes.exception.NotFoundException;
import com.njangi.groupes.repository.GroupeRepository;
import com.njangi.groupes.repository.SessionTontineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SessionTontineService {

    private static final Logger log = LoggerFactory.getLogger(SessionTontineService.class);

    private final SessionTontineRepository sessionRepository;
    private final GroupeRepository groupeRepository;
    private final GroupeEventPublisher eventPublisher;

    public SessionTontineService(SessionTontineRepository sessionRepository,
                                 GroupeRepository groupeRepository,
                                 GroupeEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.groupeRepository = groupeRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<SessionTontineDto> getSessions(UUID groupeId) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe", groupeId);
        }
        return sessionRepository.findByGroupeIdOrderByDateDebutDesc(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    public Optional<SessionTontineDto> getSessionActive(UUID groupeId) {
        return sessionRepository.findByGroupeIdAndStatut(groupeId, StatutSession.EN_COURS)
                .map(this::toDto);
    }

    @Transactional
    public SessionTontineDto creerSession(UUID groupeId, CreerSessionRequest req) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe", groupeId);
        }

        SessionTontine session = new SessionTontine();
        session.setGroupeId(groupeId);
        session.setLibelle(req.libelle());
        session.setDateDebut(req.dateDebut());
        session.setDateFin(req.dateFin());
        session.setMontantCagnotteParSeance(req.montantCagnotteParSeance());
        session.setNombreToursTotal(req.nombreToursTotal());
        session.setStatut(StatutSession.PLANIFIEE);

        SessionTontine saved = sessionRepository.save(session);
        log.info("Session de tontine planifiee : id={}, groupe={}, libelle='{}'",
                saved.getId(), groupeId, saved.getLibelle());

        eventPublisher.publierSessionCreee(groupeId, saved.getId(), saved.getLibelle());

        return toDto(saved);
    }

    @Transactional
    public SessionTontineDto demarrerSession(UUID sessionId) {
        SessionTontine session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("SessionTontine", sessionId));

        if (session.getStatut() == StatutSession.EN_COURS) {
            throw new BusinessException("Cette session est deja en cours");
        }
        if (session.getStatut() == StatutSession.CLOTUREE) {
            throw new BusinessException("Impossible de demarrer une session deja cloturee");
        }

        // Règle métier : une seule session EN_COURS à la fois par groupe
        Optional<SessionTontine> sessionEnCours = sessionRepository.findByGroupeIdAndStatut(
                session.getGroupeId(), StatutSession.EN_COURS);
        if (sessionEnCours.isPresent()) {
            throw new BusinessException("Une autre session est deja en cours pour ce groupe (Session ID: "
                    + sessionEnCours.get().getId() + ")");
        }

        session.setStatut(StatutSession.EN_COURS);
        SessionTontine updated = sessionRepository.save(session);
        log.info("Session demarree avec succes : id={}", sessionId);

        eventPublisher.publierSessionDemarree(updated.getGroupeId(), updated.getId());

        return toDto(updated);
    }

    @Transactional
    public SessionTontineDto cloturerSession(UUID sessionId) {
        SessionTontine session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("SessionTontine", sessionId));

        if (session.getStatut() == StatutSession.CLOTUREE) {
            throw new BusinessException("La session est deja cloturee");
        }

        session.setStatut(StatutSession.CLOTUREE);
        SessionTontine updated = sessionRepository.save(session);
        log.info("Session cloturee : id={}", sessionId);

        eventPublisher.publierSessionCloturee(updated.getGroupeId(), updated.getId());

        return toDto(updated);
    }

    public SessionTontineDto toDto(SessionTontine s) {
        return new SessionTontineDto(
                s.getId(),
                s.getGroupeId(),
                s.getLibelle(),
                s.getDateDebut(),
                s.getDateFin(),
                s.getMontantCagnotteParSeance(),
                s.getNombreToursTotal(),
                s.getStatut(),
                s.getCreatedAt(),
                s.getUpdatedAt()
        );
    }
}
