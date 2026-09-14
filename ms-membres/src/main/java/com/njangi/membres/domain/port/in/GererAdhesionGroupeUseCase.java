package com.njangi.membres.domain.port.in;

import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.model.RoleMembre;
import java.util.List;
import java.util.UUID;

public interface GererAdhesionGroupeUseCase {
    AdhesionGroupe adhererAuGroupe(UUID utilisateurId, UUID groupeId, RoleMembre role);
    AdhesionGroupe changerRole(UUID adhesionId, RoleMembre nouveauRole);
    List<AdhesionGroupe> listerMembresGroupe(UUID groupeId);
    List<AdhesionGroupe> listerGroupesUtilisateur(UUID utilisateurId);
}
