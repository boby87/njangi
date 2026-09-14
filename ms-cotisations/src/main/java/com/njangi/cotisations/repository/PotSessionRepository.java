package com.njangi.cotisations.repository;

import com.njangi.cotisations.entity.PotSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PotSessionRepository extends JpaRepository<PotSession, UUID> {

    List<PotSession> findByGroupeId(UUID groupeId);

    List<PotSession> findBySessionTontineId(UUID sessionTontineId);

    List<PotSession> findBySessionTontineIdOrderByOrdrePassageAsc(UUID sessionTontineId);

    Optional<PotSession> findBySessionTontineIdAndOrdrePassage(UUID sessionTontineId, int ordrePassage);

    Optional<PotSession> findByReunionId(UUID reunionId);

    List<PotSession> findByMembreBeneficiaireId(UUID membreBeneficiaireId);
}
