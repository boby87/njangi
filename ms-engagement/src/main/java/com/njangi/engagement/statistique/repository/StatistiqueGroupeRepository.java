package com.njangi.engagement.statistique.repository;

import com.njangi.engagement.statistique.entity.StatistiqueGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StatistiqueGroupeRepository extends JpaRepository<StatistiqueGroupe, UUID> {

    List<StatistiqueGroupe> findByGroupeIdOrderByCalculeLeDesc(UUID groupeId);

    Optional<StatistiqueGroupe> findByGroupeIdAndSessionId(UUID groupeId, UUID sessionId);

    Optional<StatistiqueGroupe> findTopByGroupeIdOrderByCalculeLeDesc(UUID groupeId);

    boolean existsByGroupeIdAndSessionId(UUID groupeId, UUID sessionId);
}
