package com.njangi.auth.repository;

import com.njangi.auth.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByTelephone(String telephone);

    Optional<Utilisateur> findByEmail(String email);

    @Query("SELECT u FROM Utilisateur u WHERE u.telephone = :identifiant OR u.email = :identifiant")
    Optional<Utilisateur> findByIdentifiant(@Param("identifiant") String identifiant);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    Optional<Utilisateur> findByProviderAuthAndProviderId(String providerAuth, String providerId);
}
