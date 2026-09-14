package com.njangi.penalites.repository;

import com.njangi.penalites.entity.TarificationPenalite;
import com.njangi.penalites.entity.TypeInfraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TarificationPenaliteRepository extends JpaRepository<TarificationPenalite, UUID> {

    Optional<TarificationPenalite> findByGroupeIdAndTypeInfraction(UUID groupeId, TypeInfraction typeInfraction);

    Optional<TarificationPenalite> findByGroupeIdAndTypeInfractionAndActifTrue(UUID groupeId, TypeInfraction typeInfraction);

    List<TarificationPenalite> findByGroupeId(UUID groupeId);

    List<TarificationPenalite> findByGroupeIdAndActifTrue(UUID groupeId);
}
