package com.njangi.gouvernance.repository;

import com.njangi.gouvernance.entity.GroupeMembre;
import com.njangi.gouvernance.entity.StatutMembreGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupeMembreRepository extends JpaRepository<GroupeMembre, UUID> {

    List<GroupeMembre> findByGroupeId(UUID groupeId);

    List<GroupeMembre> findByMembreId(UUID membreId);

    Optional<GroupeMembre> findByGroupeIdAndMembreId(UUID groupeId, UUID membreId);

    boolean existsByGroupeIdAndMembreId(UUID groupeId, UUID membreId);

    long countByGroupeIdAndStatut(UUID groupeId, StatutMembreGroupe statut);
}
