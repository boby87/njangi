package com.njangi.membres.infrastructure.adapter.out.persistence.adapter;

import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.port.out.AdhesionRepositoryPort;
import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreGroupeJpaEntity;
import com.njangi.membres.infrastructure.adapter.out.persistence.mapper.MembreJpaMapper;
import com.njangi.membres.infrastructure.adapter.out.persistence.repository.SpringDataMembreGroupeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AdhesionJpaAdapter implements AdhesionRepositoryPort {

    private final SpringDataMembreGroupeRepository repository;

    public AdhesionJpaAdapter(SpringDataMembreGroupeRepository repository) {
        this.repository = repository;
    }

    @Override
    public AdhesionGroupe sauvegarder(AdhesionGroupe adhesion) {
        MembreGroupeJpaEntity entity = MembreJpaMapper.toJpaEntity(adhesion);
        MembreGroupeJpaEntity saved = repository.save(entity);
        return MembreJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<AdhesionGroupe> trouverParId(UUID id) {
        return repository.findById(id).map(MembreJpaMapper::toDomain);
    }

    @Override
    public Optional<AdhesionGroupe> trouverParUtilisateurEtGroupe(UUID utilisateurId, UUID groupeId) {
        return repository.findByUtilisateurIdAndGroupeId(utilisateurId, groupeId)
                .map(MembreJpaMapper::toDomain);
    }

    @Override
    public List<AdhesionGroupe> trouverParGroupeId(UUID groupeId) {
        return repository.findByGroupeId(groupeId).stream()
                .map(MembreJpaMapper::toDomain)
                .toList();
    }

    @Override
    public List<AdhesionGroupe> trouverParUtilisateurId(UUID utilisateurId) {
        return repository.findByUtilisateurId(utilisateurId).stream()
                .map(MembreJpaMapper::toDomain)
                .toList();
    }
}
