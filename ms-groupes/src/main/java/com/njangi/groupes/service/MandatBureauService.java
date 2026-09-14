package com.njangi.groupes.service;

import com.njangi.groupes.dto.ElireBureauRequest;
import com.njangi.groupes.dto.MandatBureauDto;
import com.njangi.groupes.entity.*;
import com.njangi.groupes.event.GroupeEventPublisher;
import com.njangi.groupes.exception.NotFoundException;
import com.njangi.groupes.repository.GroupeMembreRepository;
import com.njangi.groupes.repository.GroupeRepository;
import com.njangi.groupes.repository.MandatBureauRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class MandatBureauService {

    private static final Logger log = LoggerFactory.getLogger(MandatBureauService.class);

    private final MandatBureauRepository mandatRepository;
    private final GroupeRepository groupeRepository;
    private final GroupeMembreRepository groupeMembreRepository;
    private final GroupeEventPublisher eventPublisher;

    public MandatBureauService(MandatBureauRepository mandatRepository,
                               GroupeRepository groupeRepository,
                               GroupeMembreRepository groupeMembreRepository,
                               GroupeEventPublisher eventPublisher) {
        this.mandatRepository = mandatRepository;
        this.groupeRepository = groupeRepository;
        this.groupeMembreRepository = groupeMembreRepository;
        this.eventPublisher = eventPublisher;
    }

    public Optional<MandatBureauDto> getMandatActif(UUID groupeId) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe", groupeId);
        }
        return mandatRepository.findByGroupeIdAndActifTrue(groupeId).map(this::toDto);
    }

    public List<MandatBureauDto> getHistoriqueMandats(UUID groupeId) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe", groupeId);
        }
        return mandatRepository.findByGroupeIdOrderByDateDebutDesc(groupeId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public MandatBureauDto elireBureau(UUID groupeId, ElireBureauRequest req) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new NotFoundException("Groupe", groupeId));

        // Clôture du mandat précédent actif s'il existe
        mandatRepository.findByGroupeIdAndActifTrue(groupeId).ifPresent(ancien -> {
            ancien.setActif(false);
            ancien.setStatut(StatutMandat.EXPIRE);
            ancien.setDateFin(req.dateDebut() != null ? req.dateDebut() : LocalDate.now());
            mandatRepository.save(ancien);
            log.info("Ancien mandat bureau {} expire pour le groupe {}", ancien.getId(), groupeId);
        });

        // Enregistrement du nouveau mandat élu
        MandatBureau nouveauMandat = new MandatBureau();
        nouveauMandat.setGroupeId(groupeId);
        nouveauMandat.setPresidentMembreId(req.presidentMembreId());
        nouveauMandat.setTresorierMembreId(req.tresorierMembreId());
        nouveauMandat.setSecretaireMembreId(req.secretaireMembreId());
        nouveauMandat.setVicePresidentMembreId(req.vicePresidentMembreId());
        nouveauMandat.setAuditeurMembreId(req.auditeurMembreId());
        nouveauMandat.setDateDebut(req.dateDebut() != null ? req.dateDebut() : LocalDate.now());
        nouveauMandat.setDateFin(req.dateFin());
        nouveauMandat.setActif(true);
        nouveauMandat.setStatut(StatutMandat.EN_COURS);

        MandatBureau saved = mandatRepository.save(nouveauMandat);

        // =========================================================================
        // RÈGLE MÉTIER CRITIQUE (GEMINI.md) :
        // Le Créateur perd ses prérogatives techniques initiales et est rétrogradé
        // en MEMBRE simple dès l'élection du premier bureau.
        // =========================================================================
        UUID createurId = groupe.getCreateurMembreId();
        UUID createurARetrograder = null;

        // Mise à jour des rôles dans groupe_membre
        attribuerRole(groupeId, req.presidentMembreId(), RoleMembre.PRESIDENT);
        attribuerRole(groupeId, req.tresorierMembreId(), RoleMembre.TRESORIER);
        attribuerRole(groupeId, req.secretaireMembreId(), RoleMembre.SECRETAIRE);
        if (req.auditeurMembreId() != null) {
            attribuerRole(groupeId, req.auditeurMembreId(), RoleMembre.AUDITEUR);
        }

        // Si le créateur n'est pas le président, ni le trésorier, ni le secrétaire élu :
        boolean createurEstDansLeBureau = createurId.equals(req.presidentMembreId())
                || createurId.equals(req.tresorierMembreId())
                || createurId.equals(req.secretaireMembreId());

        if (!createurEstDansLeBureau) {
            Optional<GroupeMembre> adhesionCreateur = groupeMembreRepository.findByGroupeIdAndMembreId(groupeId, createurId);
            if (adhesionCreateur.isPresent() && adhesionCreateur.get().getRole() == RoleMembre.CREATEUR) {
                GroupeMembre gmCreateur = adhesionCreateur.get();
                gmCreateur.setRole(RoleMembre.MEMBRE);
                groupeMembreRepository.save(gmCreateur);
                createurARetrograder = createurId;
                log.info("Créateur {} rétrogradé en MEMBRE suite à l'élection du bureau pour le groupe {}",
                        createurId, groupeId);
            }
        }

        // Notification Kafka
        eventPublisher.publierBureauElu(
                groupeId,
                req.presidentMembreId(),
                req.tresorierMembreId(),
                req.secretaireMembreId(),
                createurARetrograder
        );

        return toDto(saved);
    }

    private void attribuerRole(UUID groupeId, UUID membreId, RoleMembre role) {
        if (membreId == null) return;
        groupeMembreRepository.findByGroupeIdAndMembreId(groupeId, membreId).ifPresentOrElse(gm -> {
            gm.setRole(role);
            groupeMembreRepository.save(gm);
        }, () -> {
            GroupeMembre gm = new GroupeMembre();
            gm.setGroupeId(groupeId);
            gm.setMembreId(membreId);
            gm.setRole(role);
            gm.setStatut(StatutMembreGroupe.ACTIF);
            groupeMembreRepository.save(gm);
        });
    }

    public MandatBureauDto toDto(MandatBureau m) {
        return new MandatBureauDto(
                m.getId(),
                m.getGroupeId(),
                m.getPresidentMembreId(),
                m.getTresorierMembreId(),
                m.getSecretaireMembreId(),
                m.getVicePresidentMembreId(),
                m.getAuditeurMembreId(),
                m.getDateDebut(),
                m.getDateFin(),
                m.getActif(),
                m.getStatut(),
                m.getCreatedAt(),
                m.getUpdatedAt()
        );
    }
}
