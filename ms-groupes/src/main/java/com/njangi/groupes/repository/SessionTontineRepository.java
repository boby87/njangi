package com.njangi.groupes.repository;

import com.njangi.groupes.entity.SessionTontine;
import com.njangi.groupes.entity.StatutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionTontineRepository extends JpaRepository<SessionTontine, UUID> {

    List<SessionTontine> findByGroupeIdOrderByDateDebutDesc(UUID groupeId);

    Optional<SessionTontine> findByGroupeIdAndStatut(UUID groupeId, StatutSession statut);

    List<SessionTontine> findByGroupeIdAndStatutOrderByDateDebutDesc(UUID groupeId, StatutSession statut);
}
