package com.njangi.gouvernance.service;

import com.njangi.gouvernance.dto.*;
import com.njangi.gouvernance.entity.*;
import com.njangi.gouvernance.event.GouvernanceEventPublisher;
import com.njangi.gouvernance.exception.BusinessException;
import com.njangi.gouvernance.exception.NotFoundException;
import com.njangi.gouvernance.repository.GroupeMembreRepository;
import com.njangi.gouvernance.repository.GroupeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GroupeService {

    private static final Logger log = LoggerFactory.getLogger(GroupeService.class);

    private final GroupeRepository groupeRepository;
    private final GroupeMembreRepository groupeMembreRepository;
    private final GouvernanceEventPublisher eventPublisher;

    public GroupeService(GroupeRepository groupeRepository,
                         GroupeMembreRepository groupeMembreRepository,
                         GouvernanceEventPublisher eventPublisher) {
        this.groupeRepository = groupeRepository;
        this.groupeMembreRepository = groupeMembreRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<GroupeDto> findAll() {
        return groupeRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<GroupeDto> findByMembre(UUID membreId) {
        if (membreId == null) {
            return List.of();
        }
        Set<UUID> groupeIds = new LinkedHashSet<>();
        groupeMembreRepository.findByMembreId(membreId).stream()
                .filter(gm -> gm.getStatut() == null || gm.getStatut() == StatutMembreGroupe.ACTIF)
                .map(GroupeMembre::getGroupeId)
                .forEach(groupeIds::add);
        groupeRepository.findByCreateurMembreId(membreId).stream()
                .map(Groupe::getId)
                .forEach(groupeIds::add);

        if (groupeIds.isEmpty()) {
            return List.of();
        }
        return groupeRepository.findAllById(groupeIds).stream().map(this::toDto).toList();
    }

    public GroupeDto findById(UUID id) {
        return groupeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Groupe", id));
    }

    public GroupeDto findByCodeInvitation(String code) {
        return groupeRepository.findByCodeInvitation(code)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Groupe avec le code d'invitation " + code + " introuvable"));
    }

    public List<GroupeDto> findByCreateur(UUID createurId) {
        return groupeRepository.findByCreateurMembreId(createurId).stream().map(this::toDto).toList();
    }

    @Transactional
    public GroupeDto create(CreerGroupeRequest request) {
        String code = genererCodeInvitation(request.nom());

        Groupe groupe = new Groupe();
        groupe.setNom(request.nom());
        groupe.setDescription(request.description());
        groupe.setCreateurMembreId(request.createurMembreId());
        groupe.setTypeSiege(request.typeSiege() != null ? request.typeSiege() : TypeSiege.FIXE);
        groupe.setAdresseSiege(request.adresseSiege());
        groupe.setMontantCotisationPrincipale(request.montantCotisationPrincipale());
        groupe.setFrequenceReunion(request.frequenceReunion());
        groupe.setNombreMembresMax(request.nombreMembresMax());
        groupe.setCodeInvitation(code);
        groupe.setReglementInterieur(request.reglementInterieur());
        groupe.setStatut(StatutGroupe.ACTIF);

        Groupe saved = groupeRepository.save(groupe);

        // Attribution automatique du créateur comme premier membre avec le rôle technique CREATEUR
        GroupeMembre createurAdhesion = new GroupeMembre();
        createurAdhesion.setGroupeId(saved.getId());
        createurAdhesion.setMembreId(request.createurMembreId());
        createurAdhesion.setRole(RoleMembre.CREATEUR);
        createurAdhesion.setStatut(StatutMembreGroupe.ACTIF);
        createurAdhesion.setDateAdhesion(LocalDateTime.now());
        groupeMembreRepository.save(createurAdhesion);

        log.info("Groupe créé avec succès : id={}, nom='{}', code='{}'", saved.getId(), saved.getNom(), code);
        eventPublisher.publierGroupeCree(saved.getId(), saved.getNom(), saved.getCreateurMembreId(), saved.getTypeSiege().name());

        return toDto(saved);
    }

    @Transactional
    public GroupeDto update(UUID id, ModifierGroupeRequest request) {
        Groupe groupe = groupeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Groupe", id));

        if (groupe.getStatut() == StatutGroupe.CLOTURE) {
            throw new BusinessException("Impossible de modifier un groupe clôturé");
        }

        groupe.setNom(request.nom());
        groupe.setDescription(request.description());
        groupe.setTypeSiege(request.typeSiege());
        groupe.setAdresseSiege(request.adresseSiege());
        groupe.setMontantCotisationPrincipale(request.montantCotisationPrincipale());
        groupe.setFrequenceReunion(request.frequenceReunion());
        groupe.setNombreMembresMax(request.nombreMembresMax());
        groupe.setReglementInterieur(request.reglementInterieur());

        Groupe updated = groupeRepository.save(groupe);
        log.info("Groupe mis à jour : id={}", id);
        return toDto(updated);
    }

    @Transactional
    public GroupeMembreDto ajouterMembre(UUID groupeId, AjouterMembreRequest request) {
        Groupe groupe = groupeRepository.findById(groupeId)
                .orElseThrow(() -> new NotFoundException("Groupe", groupeId));

        if (groupe.getStatut() != StatutGroupe.ACTIF) {
            throw new BusinessException("Ce groupe n'est pas actif");
        }

        if (groupeMembreRepository.existsByGroupeIdAndMembreId(groupeId, request.membreId())) {
            throw new BusinessException("Le membre fait déjà partie de ce groupe");
        }

        if (groupe.getNombreMembresMax() != null) {
            long effectifActuel = groupeMembreRepository.countByGroupeIdAndStatut(groupeId, StatutMembreGroupe.ACTIF);
            if (effectifActuel >= groupe.getNombreMembresMax()) {
                throw new BusinessException("Le quota maximal de membres pour ce groupe est atteint (" + groupe.getNombreMembresMax() + ")");
            }
        }

        GroupeMembre gm = new GroupeMembre();
        gm.setGroupeId(groupeId);
        gm.setMembreId(request.membreId());
        gm.setRole(request.role() != null ? request.role() : RoleMembre.MEMBRE);
        gm.setStatut(StatutMembreGroupe.ACTIF);
        gm.setDateAdhesion(LocalDateTime.now());

        GroupeMembre saved = groupeMembreRepository.save(gm);
        log.info("Membre {} ajouté au groupe {} avec le rôle {}", request.membreId(), groupeId, saved.getRole());

        eventPublisher.publierMembreAjoute(groupeId, request.membreId(), saved.getRole().name());
        return toMembreDto(saved);
    }

    @Transactional
    public GroupeMembreDto rejoindreParCode(String codeInvitation, UUID membreId) {
        GroupeDto groupe = findByCodeInvitation(codeInvitation);
        return ajouterMembre(groupe.id(), new AjouterMembreRequest(membreId, RoleMembre.MEMBRE));
    }

    public List<GroupeMembreDto> getMembres(UUID groupeId) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe", groupeId);
        }
        return groupeMembreRepository.findByGroupeId(groupeId).stream().map(this::toMembreDto).toList();
    }

    private String genererCodeInvitation(String nom) {
        String base = nom.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (base.length() > 6) {
            base = base.substring(0, 6);
        }
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "NJG-" + base + "-" + suffix;
    }

    public GroupeDto toDto(Groupe g) {
        return new GroupeDto(
                g.getId(),
                g.getNom(),
                g.getDescription(),
                g.getCreateurMembreId(),
                g.getTypeSiege(),
                g.getAdresseSiege(),
                g.getMontantCotisationPrincipale(),
                g.getFrequenceReunion(),
                g.getNombreMembresMax(),
                g.getCodeInvitation(),
                g.getReglementInterieur(),
                g.getStatut(),
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }

    public GroupeMembreDto toMembreDto(GroupeMembre gm) {
        return new GroupeMembreDto(
                gm.getId(),
                gm.getGroupeId(),
                gm.getMembreId(),
                gm.getRole(),
                gm.getStatut(),
                gm.getDateAdhesion(),
                gm.getDateFin()
        );
    }
}
