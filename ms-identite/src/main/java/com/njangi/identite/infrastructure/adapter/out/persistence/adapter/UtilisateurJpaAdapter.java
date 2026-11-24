package com.njangi.identite.infrastructure.adapter.out.persistence.adapter;

import com.njangi.identite.domain.model.Utilisateur;
import com.njangi.identite.domain.port.out.UtilisateurRepositoryPort;
import com.njangi.identite.infrastructure.adapter.out.persistence.entity.UtilisateurJpaEntity;
import com.njangi.identite.infrastructure.adapter.out.persistence.mapper.AuthJpaMapper;
import com.njangi.identite.infrastructure.adapter.out.persistence.repository.SpringDataUtilisateurRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UtilisateurJpaAdapter implements UtilisateurRepositoryPort {

    private final SpringDataUtilisateurRepository repository;

    public UtilisateurJpaAdapter(SpringDataUtilisateurRepository repository) {
        this.repository = repository;
    }

    @Override
    public Utilisateur sauvegarder(Utilisateur utilisateur) {
        UtilisateurJpaEntity entity = AuthJpaMapper.toJpaEntity(utilisateur);
        UtilisateurJpaEntity saved = repository.save(entity);
        return AuthJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<Utilisateur> trouverParId(UUID id) {
        return repository.findById(id).map(AuthJpaMapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> trouverParTelephone(String telephone) {
        return repository.findByTelephone(telephone).map(AuthJpaMapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> trouverParEmail(String email) {
        return repository.findByEmail(email).map(AuthJpaMapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> trouverParIdentifiant(String identifiant) {
        return repository.findByIdentifiant(identifiant).map(AuthJpaMapper::toDomain);
    }

    @Override
    public Optional<Utilisateur> trouverParProviderAuthEtProviderId(String providerAuth, String providerId) {
        return repository.findByProviderAuthAndProviderId(providerAuth, providerId)
                .map(AuthJpaMapper::toDomain);
    }

    @Override
    public boolean existeParTelephone(String telephone) {
        return repository.existsByTelephone(telephone);
    }

    @Override
    public boolean existeParEmail(String email) {
        return repository.existsByEmail(email);
    }
}
