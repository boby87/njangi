package com.njangi.groupes.repository;

import com.njangi.groupes.entity.Groupe;
import com.njangi.groupes.entity.StatutGroupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, UUID> {

    Optional<Groupe> findByCodeInvitation(String codeInvitation);

    List<Groupe> findByCreateurMembreId(UUID createurMembreId);

    List<Groupe> findByStatut(StatutGroupe statut);

    boolean existsByCodeInvitation(String codeInvitation);
}
