package com.njangi.groupes.service;

import com.njangi.groupes.dto.*;
import com.njangi.groupes.entity.*;
import com.njangi.groupes.exception.BusinessException;
import com.njangi.groupes.exception.NotFoundException;
import com.njangi.groupes.repository.GroupeMembreRepository;
import com.njangi.groupes.repository.GroupeRepository;
import com.njangi.groupes.event.GroupeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GroupeService {

    private final GroupeRepository groupeRepository;
    private final GroupeMembreRepository groupeMembreRepository;
    private final GroupeEventPublisher eventPublisher;

    public List<GroupeDto> findAll() {
        return groupeRepository.findAll().stream().map(this::toDto).toList();
    }

    public GroupeDto findById(UUID id) {
        return groupeRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Groupe introuvable : " + id));
    }

    @Transactional
    public GroupeDto create(CreateGroupeRequest request) {
        Groupe groupe = Groupe.builder()
                .nom(request.nom())
                .description(request.description())
                .createurMembreId(request.createurMembreId())
                .typeSiege(request.typeSiege())
                .adresseSiege(request.adresseSiege())
                .montantCotisationPrincipale(request.montantCotisationPrincipale())
                .frequenceReunion(request.frequenceReunion())
                .nombreMembresMax(request.nombreMembresMax())
                .build();

        Groupe saved = groupeRepository.save(groupe);

        // Le createur devient membre du groupe avec le role CREATEUR
        GroupeMembre createur = GroupeMembre.builder()
                .groupeId(saved.getId())
                .membreId(request.createurMembreId())
                .role(RoleMembre.CREATEUR)
                .build();
        groupeMembreRepository.save(createur);

        eventPublisher.publishGroupeCree(saved);
        log.info("Groupe cree : {}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public GroupeMembreDto ajouterMembre(UUID groupeId, AjouterMembreRequest request) {
        if (!groupeRepository.existsById(groupeId)) {
            throw new NotFoundException("Groupe introuvable : " + groupeId);
        }
        if (groupeMembreRepository.existsByGroupeIdAndMembreId(groupeId, request.membreId())) {
            throw new BusinessException("Le membre est deja dans ce groupe");
        }

        GroupeMembre membre = GroupeMembre.builder()
                .groupeId(groupeId)
                .membreId(request.membreId())
                .role(request.role())
                .build();

        GroupeMembre saved = groupeMembreRepository.save(membre);
        log.info("Membre {} ajoute au groupe {}", request.membreId(), groupeId);
        return toMembreDto(saved);
    }

    public List<GroupeMembreDto> getMembres(UUID groupeId) {
        return groupeMembreRepository.findByGroupeId(groupeId).stream()
                .map(this::toMembreDto).toList();
    }

    private GroupeDto toDto(Groupe g) {
        return new GroupeDto(g.getId(), g.getNom(), g.getDescription(), g.getCreateurMembreId(),
                g.getTypeSiege(), g.getAdresseSiege(), g.getMontantCotisationPrincipale(),
                g.getFrequenceReunion(), g.getNombreMembresMax(), g.getStatut(), g.getCreatedAt());
    }

    private GroupeMembreDto toMembreDto(GroupeMembre gm) {
        return new GroupeMembreDto(gm.getId(), gm.getGroupeId(), gm.getMembreId(),
                gm.getRole(), gm.getStatut(), gm.getDateAdhesion());
    }
}
