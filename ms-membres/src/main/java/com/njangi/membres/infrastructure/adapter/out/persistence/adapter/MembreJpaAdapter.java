package com.njangi.membres.infrastructure.adapter.out.persistence.adapter;

import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.MembreId;
import com.njangi.membres.domain.port.out.MembreRepositoryPort;
import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreJpaEntity;
import com.njangi.membres.infrastructure.adapter.out.persistence.mapper.MembreJpaMapper;
import com.njangi.membres.infrastructure.adapter.out.persistence.repository.SpringDataMembreGroupeRepository;
import com.njangi.membres.infrastructure.adapter.out.persistence.repository.SpringDataMembreRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MembreJpaAdapter implements MembreRepositoryPort {

    private final SpringDataMembreRepository membreRepository;
    private final SpringDataMembreGroupeRepository membreGroupeRepository;

    public MembreJpaAdapter(SpringDataMembreRepository membreRepository, SpringDataMembreGroupeRepository membreGroupeRepository) {
        this.membreRepository = membreRepository;
        this.membreGroupeRepository = membreGroupeRepository;
    }

    @Override
    public Membre sauvegarder(Membre membre) {
        MembreJpaEntity entity = MembreJpaMapper.toJpaEntity(membre);
        MembreJpaEntity saved = membreRepository.save(entity);
        return MembreJpaMapper.toDomain(saved, membre.getAdhesions());
    }

    @Override
    public Optional<Membre> trouverParId(MembreId id) {
        return membreRepository.findById(id.value())
                .map(entity -> {
                    List<AdhesionGroupe> adhesions = membreGroupeRepository.findByUtilisateurId(entity.getId())
                            .stream().map(MembreJpaMapper::toDomain).toList();
                    return MembreJpaMapper.toDomain(entity, adhesions);
                });
    }

    @Override
    public Optional<Membre> trouverParAuthId(UUID authUtilisateurId) {
        return membreRepository.findByAuthUtilisateurId(authUtilisateurId)
                .map(entity -> {
                    List<AdhesionGroupe> adhesions = membreGroupeRepository.findByUtilisateurId(entity.getId())
                            .stream().map(MembreJpaMapper::toDomain).toList();
                    return MembreJpaMapper.toDomain(entity, adhesions);
                });
    }

    @Override
    public Optional<Membre> trouverParEmail(String email) {
        return membreRepository.findByEmail(email)
                .map(entity -> MembreJpaMapper.toDomain(entity, List.of()));
    }

    @Override
    public Optional<Membre> trouverParTelephone(String telephone) {
        return membreRepository.findByTelephone(telephone)
                .map(entity -> MembreJpaMapper.toDomain(entity, List.of()));
    }

    @Override
    public List<Membre> trouverTous() {
        return membreRepository.findAll().stream()
                .map(entity -> {
                    List<AdhesionGroupe> adhesions = membreGroupeRepository.findByUtilisateurId(entity.getId())
                            .stream().map(MembreJpaMapper::toDomain).toList();
                    return MembreJpaMapper.toDomain(entity, adhesions);
                })
                .toList();
    }

    @Override
    public boolean existeParEmail(String email) {
        return membreRepository.existsByEmail(email);
    }

    @Override
    public boolean existeParTelephone(String telephone) {
        return membreRepository.existsByTelephone(telephone);
    }
}
