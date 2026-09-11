package com.njangi.groupes.repository;
import com.njangi.groupes.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {
    List<Session> findByGroupeId(UUID groupeId);
    Optional<Session> findByGroupeIdAndStatut(UUID groupeId, Session.StatutSession statut);
}
