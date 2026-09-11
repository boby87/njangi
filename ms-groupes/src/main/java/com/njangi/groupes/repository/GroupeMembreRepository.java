package com.njangi.groupes.repository;

import com.njangi.groupes.entity.GroupeMembre;
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
}
