package com.njangi.membres.domain.port.out;

import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.MembreId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembreRepositoryPort {
    Membre sauvegarder(Membre membre);
    Optional<Membre> trouverParId(MembreId id);
    Optional<Membre> trouverParAuthId(UUID authUtilisateurId);
    Optional<Membre> trouverParEmail(String email);
    Optional<Membre> trouverParTelephone(String telephone);
    List<Membre> trouverTous();
    boolean existeParEmail(String email);
    boolean existeParTelephone(String telephone);
}
