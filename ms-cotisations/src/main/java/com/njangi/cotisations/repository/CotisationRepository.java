package com.njangi.cotisations.repository;

import com.njangi.cotisations.entity.Cotisation;
import com.njangi.cotisations.entity.StatutCotisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, UUID> {

    List<Cotisation> findByMembreId(UUID membreId);

    List<Cotisation> findByGroupeId(UUID groupeId);

    List<Cotisation> findByMembreIdAndStatut(UUID membreId, StatutCotisation statut);

    List<Cotisation> findByReunionId(UUID reunionId);
}
