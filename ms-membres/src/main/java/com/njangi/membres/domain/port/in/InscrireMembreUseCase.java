package com.njangi.membres.domain.port.in;

import com.njangi.membres.domain.model.Membre;
import java.time.LocalDate;
import java.util.UUID;

public interface InscrireMembreUseCase {
    Membre inscrire(UUID authUtilisateurId, String nom, String prenom,
                    String email, String telephone, LocalDate dateNaissance,
                    String adresse, String ville);
}
