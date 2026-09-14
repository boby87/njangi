package com.njangi.membres.application.service;

import com.njangi.membres.domain.event.RoleAssigneeEvent;
import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.model.RoleMembre;
import com.njangi.membres.domain.model.StatutMembre;
import com.njangi.membres.domain.port.in.GererAdhesionGroupeUseCase;
import com.njangi.membres.domain.port.out.AdhesionRepositoryPort;
import com.njangi.membres.domain.port.out.MembreEventPublisherPort;
import com.njangi.membres.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GererAdhesionGroupeService implements GererAdhesionGroupeUseCase {

    private final AdhesionRepositoryPort adhesionRepositoryPort;
    private final MembreEventPublisherPort eventPublisherPort;

    public GererAdhesionGroupeService(AdhesionRepositoryPort adhesionRepositoryPort, MembreEventPublisherPort eventPublisherPort) {
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

        eventPublisherPort.publierRoleAssignee(new RoleAssigneeEvent(
                maj.getId(),
                maj.getUtilisateurId(),
                maj.getGroupeId(),
                maj.getRole()
        ));

        return maj;
    }

    @Override
    public List<AdhesionGroupe> listerMembresGroupe(UUID groupeId) {
        return adhesionRepositoryPort.trouverParGroupeId(groupeId);
    }

    @Override
    public List<AdhesionGroupe> listerGroupesUtilisateur(UUID utilisateurId) {
        return adhesionRepositoryPort.trouverParUtilisateurId(utilisateurId);
    }
}
