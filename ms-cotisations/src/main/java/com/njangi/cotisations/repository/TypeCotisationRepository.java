package com.njangi.cotisations.repository;

import com.njangi.cotisations.entity.TypeCotisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TypeCotisationRepository extends JpaRepository<TypeCotisation, UUID> {

    List<TypeCotisation> findByGroupeId(UUID groupeId);
}
