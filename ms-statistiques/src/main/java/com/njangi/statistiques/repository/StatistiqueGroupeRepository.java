package com.njangi.statistiques.repository;

import com.njangi.statistiques.entity.StatistiqueGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatistiqueGroupeRepository extends JpaRepository<StatistiqueGroupe, UUID> {

    List<StatistiqueGroupe> findByGroupeIdOrderByCalculeLe(UUID groupeId);

    Optional<StatistiqueGroupe> findByGroupeIdAndSessionId(UUID groupeId, UUID sessionId);

    @Query("SELECT s FROM StatistiqueGroupe s WHERE s.groupeId = :groupeId ORDER BY s.calculeLe DESC")
    List<StatistiqueGroupe> findLatestByGroupe(@Param("groupeId") UUID groupeId);

    boolean existsByGroupeIdAndSessionId(UUID groupeId, UUID sessionId);
}
