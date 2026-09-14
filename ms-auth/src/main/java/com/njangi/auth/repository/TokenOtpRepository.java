package com.njangi.auth.repository;

import com.njangi.auth.entity.TokenOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenOtpRepository extends JpaRepository<TokenOtp, UUID> {

    @Query("SELECT t FROM TokenOtp t WHERE t.identifiant = :identifiant AND t.utilise = false ORDER BY t.creeLe DESC LIMIT 1")
    Optional<TokenOtp> findLatestUnusedByIdentifiant(@Param("identifiant") String identifiant);

    Optional<TokenOtp> findTopByIdentifiantAndUtiliseFalseOrderByCreeLeDesc(String identifiant);
}
