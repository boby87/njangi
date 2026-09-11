package com.njangi.paiements.repository;

import com.njangi.paiements.entity.Paiement;
import com.njangi.paiements.entity.StatutPaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID> {

    List<Paiement> findByCotisationId(UUID cotisationId);

    List<Paiement> findByMembreId(UUID membreId);

    List<Paiement> findByGroupeId(UUID groupeId);

    List<Paiement> findByStatut(StatutPaiement statut);

    Optional<Paiement> findByReference(String reference);
}
