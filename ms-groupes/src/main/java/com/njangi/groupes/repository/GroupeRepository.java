package com.njangi.groupes.repository;

import com.njangi.groupes.entity.Groupe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupeRepository extends JpaRepository<Groupe, UUID> {
    List<Groupe> findByCreateurMembreId(UUID createurMembreId);
}
