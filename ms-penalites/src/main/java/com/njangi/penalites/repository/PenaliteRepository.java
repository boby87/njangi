package com.njangi.penalites.repository;

import com.njangi.penalites.entity.Penalite;
import com.njangi.penalites.entity.Penalite.StatutPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenaliteRepository extends JpaRepository<Penalite, UUID> {

    List<Penalite> findByMembreId(UUID membreId);

    List<Penalite> findByGroupeId(UUID groupeId);

    List<Penalite> findByGroupeIdAndSessionId(UUID groupeId, UUID sessionId);

    List<Penalite> findByMembreIdAndStatut(UUID membreId, StatutPenalite statut);

    List<Penalite> findByGroupeIdAndStatut(UUID groupeId, StatutPenalite statut);

    boolean existsByMembreIdAndGroupeIdAndSessionIdAndTypeInfraction(
            UUID membreId, UUID groupeId, UUID sessionId, TypeInfraction typeInfraction);
}
