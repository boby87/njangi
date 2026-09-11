package com.njangi.membres.repository;

import com.njangi.membres.entity.MembreGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembreGroupeRepository extends JpaRepository<MembreGroupe, UUID> {
    List<MembreGroupe> findByUtilisateurId(UUID utilisateurId);
    List<MembreGroupe> findByGroupeId(UUID groupeId);
    Optional<MembreGroupe> findByUtilisateurIdAndGroupeId(UUID utilisateurId, UUID groupeId);
    boolean existsByUtilisateurIdAndGroupeId(UUID utilisateurId, UUID groupeId);
}
