package com.njangi.penalites.repository;

import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.StatutPenalite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenaliteRepository extends JpaRepository<Penalite, UUID> {

    List<Penalite> findByMembreId(UUID membreId);

    List<Penalite> findByGroupeId(UUID groupeId);

    List<Penalite> findBySessionId(UUID sessionId);

    List<Penalite> findByReunionId(UUID reunionId);

    List<Penalite> findByGroupeIdAndStatut(UUID groupeId, StatutPenalite statut);

    List<Penalite> findByMembreIdAndStatut(UUID membreId, StatutPenalite statut);
}
