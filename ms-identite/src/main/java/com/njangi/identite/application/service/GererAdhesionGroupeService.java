package com.njangi.identite.application.service;

import com.njangi.identite.domain.event.RoleAssigneeEvent;
import com.njangi.identite.domain.exception.NotFoundException;
import com.njangi.identite.domain.model.AdhesionGroupe;
import com.njangi.identite.domain.model.RoleMembre;
import com.njangi.identite.domain.model.StatutMembre;
import com.njangi.identite.domain.port.in.GererAdhesionGroupeUseCase;
import com.njangi.identite.domain.port.out.AdhesionRepositoryPort;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GererAdhesionGroupeService implements GererAdhesionGroupeUseCase {

    private final AdhesionRepositoryPort adhesionRepositoryPort;
    private final IdentiteEventPublisherPort eventPublisherPort;

    public GererAdhesionGroupeService(
            AdhesionRepositoryPort adhesionRepositoryPort,
            IdentiteEventPublisherPort eventPublisherPort
    ) {
        this.adhesionRepositoryPort = adhesionRepositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    @Transactional
    public AdhesionGroupe adhererAuGroupe(UUID utilisateurId, UUID groupeId, RoleMembre role) {
        return adhesionRepositoryPort.trouverParUtilisateurEtGroupe(utilisateurId, groupeId)
                .orElseGet(() -> {
                    AdhesionGroupe nouvelle = new AdhesionGroupe(
                            UUID.randomUUID(),
                            utilisateurId,
                            groupeId,
                            role != null ? role : RoleMembre.MEMBRE,
                            StatutMembre.ACTIF,
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    );
                    return adhesionRepositoryPort.sauvegarder(nouvelle);
                });
    }

    @Override
    @Transactional
    public AdhesionGroupe changerRole(UUID adhesionId, RoleMembre nouveauRole) {
        AdhesionGroupe adhesion = adhesionRepositoryPort.trouverParId(adhesionId)
                .orElseThrow(() -> new NotFoundException("Adhésion non trouvée pour l'ID : " + adhesionId));

        adhesion.changerRole(nouveauRole);
        AdhesionGroupe maj = adhesionRepositoryPort.sauvegarder(adhesion);

        eventPublisherPort.publishRoleAssigne(new RoleAssigneeEvent(
                maj.getId(),
                maj.getUtilisateurId(),
                maj.getGroupeId(),
                maj.getRole()
        ));

        return maj;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdhesionGroupe> listerMembresGroupe(UUID groupeId) {
        return adhesionRepositoryPort.trouverParGroupeId(groupeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdhesionGroupe> listerGroupesUtilisateur(UUID utilisateurId) {
        return adhesionRepositoryPort.trouverParUtilisateurId(utilisateurId);
    }
}
