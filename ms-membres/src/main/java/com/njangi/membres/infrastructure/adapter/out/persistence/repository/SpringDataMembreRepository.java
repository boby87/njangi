package com.njangi.membres.infrastructure.adapter.out.persistence.repository;

import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMembreRepository extends JpaRepository<MembreJpaEntity, UUID> {
    Optional<MembreJpaEntity> findByAuthUtilisateurId(UUID authUtilisateurId);
    Optional<MembreJpaEntity> findByEmail(String email);
    Optional<MembreJpaEntity> findByTelephone(String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
}
