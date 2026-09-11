package com.njangi.membres.repository;

import com.njangi.membres.entity.Membre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembreRepository extends JpaRepository<Membre, UUID> {
    Optional<Membre> findByEmail(String email);
    Optional<Membre> findByTelephone(String telephone);
    Optional<Membre> findByAuthUtilisateurId(UUID authUtilisateurId);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
