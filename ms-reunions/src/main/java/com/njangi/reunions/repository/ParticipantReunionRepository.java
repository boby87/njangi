package com.njangi.reunions.repository;

import com.njangi.reunions.entity.ParticipantReunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantReunionRepository extends JpaRepository<ParticipantReunion, UUID> {

    List<ParticipantReunion> findByReunionId(UUID reunionId);

    Optional<ParticipantReunion> findByReunionIdAndMembreId(UUID reunionId, UUID membreId);
}
