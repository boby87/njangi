package com.njangi.identite.infrastructure.adapter.out.persistence.repository;

import com.njangi.identite.infrastructure.adapter.out.persistence.entity.UtilisateurJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUtilisateurRepository extends JpaRepository<UtilisateurJpaEntity, UUID> {

    Optional<UtilisateurJpaEntity> findByTelephone(String telephone);

    Optional<UtilisateurJpaEntity> findByEmail(String email);

    @Query("SELECT u FROM UtilisateurJpaEntity u WHERE u.telephone = :identifiant OR u.email = :identifiant")
    Optional<UtilisateurJpaEntity> findByIdentifiant(@Param("identifiant") String identifiant);

    boolean existsByTelephone(String telephone);

    boolean existsByEmail(String email);

    Optional<UtilisateurJpaEntity> findByProviderAuthAndProviderId(String providerAuth, String providerId);
}
