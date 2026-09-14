package com.njangi.membres.domain.port.out;

import com.njangi.membres.domain.model.AdhesionGroupe;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdhesionRepositoryPort {
    AdhesionGroupe sauvegarder(AdhesionGroupe adhesion);
    Optional<AdhesionGroupe> trouverParId(UUID id);
    Optional<AdhesionGroupe> trouverParUtilisateurEtGroupe(UUID utilisateurId, UUID groupeId);
    List<AdhesionGroupe> trouverParGroupeId(UUID groupeId);
    List<AdhesionGroupe> trouverParUtilisateurId(UUID utilisateurId);
}
