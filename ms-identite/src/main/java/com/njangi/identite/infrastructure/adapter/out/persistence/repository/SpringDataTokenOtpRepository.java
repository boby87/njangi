package com.njangi.identite.infrastructure.adapter.out.persistence.repository;

import com.njangi.identite.infrastructure.adapter.out.persistence.entity.TokenOtpJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTokenOtpRepository extends JpaRepository<TokenOtpJpaEntity, UUID> {

    Optional<TokenOtpJpaEntity> findTopByIdentifiantAndUtiliseFalseOrderByCreeLeDesc(String identifiant);
}
