package com.njangi.gouvernance.repository;

import com.njangi.gouvernance.entity.Presence;
import com.njangi.gouvernance.entity.StatutPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PresenceRepository extends JpaRepository<Presence, UUID> {

    List<Presence> findByReunionId(UUID reunionId);

    Optional<Presence> findByReunionIdAndMembreId(UUID reunionId, UUID membreId);

    long countByReunionId(UUID reunionId);

    long countByReunionIdAndStatut(UUID reunionId, StatutPresence statut);

    void deleteByReunionIdAndMembreId(UUID reunionId, UUID membreId);
}
