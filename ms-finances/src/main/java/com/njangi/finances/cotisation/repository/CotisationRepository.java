package com.njangi.finances.cotisation.repository;

import com.njangi.finances.cotisation.entity.Cotisation;
import com.njangi.finances.cotisation.entity.StatutCotisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CotisationRepository extends JpaRepository<Cotisation, UUID> {

    List<Cotisation> findByMembreId(UUID membreId);

    List<Cotisation> findByGroupeId(UUID groupeId);

    List<Cotisation> findByMembreIdAndStatut(UUID membreId, StatutCotisation statut);

    List<Cotisation> findByReunionId(UUID reunionId);

    List<Cotisation> findByGroupeIdAndStatut(UUID groupeId, StatutCotisation statut);

    Optional<Cotisation> findByReunionIdAndMembreIdAndTypeCotisationId(UUID reunionId, UUID membreId, UUID typeCotisationId);
}
