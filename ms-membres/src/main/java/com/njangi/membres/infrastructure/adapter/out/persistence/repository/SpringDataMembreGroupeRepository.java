package com.njangi.membres.infrastructure.adapter.out.persistence.repository;

import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreGroupeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMembreGroupeRepository extends JpaRepository<MembreGroupeJpaEntity, UUID> {
    Optional<MembreGroupeJpaEntity> findByUtilisateurIdAndGroupeId(UUID utilisateurId, UUID groupeId);
    List<MembreGroupeJpaEntity> findByGroupeId(UUID groupeId);
    List<MembreGroupeJpaEntity> findByUtilisateurId(UUID utilisateurId);
}
