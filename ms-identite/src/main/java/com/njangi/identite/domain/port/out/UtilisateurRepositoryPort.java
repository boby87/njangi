package com.njangi.identite.domain.port.out;

import com.njangi.identite.domain.model.Utilisateur;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepositoryPort {
    Utilisateur sauvegarder(Utilisateur utilisateur);
    Optional<Utilisateur> trouverParId(UUID id);
    Optional<Utilisateur> trouverParTelephone(String telephone);
    Optional<Utilisateur> trouverParEmail(String email);
    Optional<Utilisateur> trouverParIdentifiant(String identifiant);
    Optional<Utilisateur> trouverParProviderAuthEtProviderId(String providerAuth, String providerId);
    boolean existeParTelephone(String telephone);
    boolean existeParEmail(String email);
}
