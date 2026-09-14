package com.njangi.groupes.repository;

import com.njangi.groupes.entity.MandatBureau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MandatBureauRepository extends JpaRepository<MandatBureau, UUID> {

    List<MandatBureau> findByGroupeIdOrderByDateDebutDesc(UUID groupeId);

    Optional<MandatBureau> findByGroupeIdAndActifTrue(UUID groupeId);
}
