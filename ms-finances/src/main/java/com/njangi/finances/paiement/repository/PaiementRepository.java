package com.njangi.finances.paiement.repository;

import com.njangi.finances.paiement.entity.Paiement;
import com.njangi.finances.paiement.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID> {

    Optional<Paiement> findByCleIdempotence(String cleIdempotence);

    Optional<Paiement> findByReference(String reference);

    List<Paiement> findByCotisationId(UUID cotisationId);

    List<Paiement> findByMembreId(UUID membreId);

    List<Paiement> findByGroupeId(UUID groupeId);

    List<Paiement> findByGroupeIdAndStatut(UUID groupeId, StatutPaiement statut);
}
