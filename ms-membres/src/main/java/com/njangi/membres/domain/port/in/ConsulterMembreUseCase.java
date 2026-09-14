package com.njangi.membres.domain.port.in;

import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.MembreId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsulterMembreUseCase {
    List<Membre> listerTous();
    Optional<Membre> trouverParId(MembreId id);
    Optional<Membre> trouverParAuthId(UUID authUtilisateurId);
    Optional<Membre> trouverParTelephone(String telephone);
    Optional<Membre> trouverParEmail(String email);
}
