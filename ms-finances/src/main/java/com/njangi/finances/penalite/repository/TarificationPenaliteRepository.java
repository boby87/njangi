package com.njangi.finances.penalite.repository;

import com.njangi.finances.penalite.entity.TarificationPenalite;
import com.njangi.finances.penalite.entity.TypeInfraction;
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
