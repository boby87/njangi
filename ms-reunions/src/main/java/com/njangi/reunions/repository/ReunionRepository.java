package com.njangi.reunions.repository;

import com.njangi.reunions.entity.Reunion;
import com.njangi.reunions.entity.StatutReunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, UUID> {

    List<Reunion> findByGroupeIdOrderByDateReunionDesc(UUID groupeId);

    List<Reunion> findByGroupeIdAndStatut(UUID groupeId, StatutReunion statut);

    List<Reunion> findBySessionTontineId(UUID sessionTontineId);

    List<Reunion> findByStatut(StatutReunion statut);
}
