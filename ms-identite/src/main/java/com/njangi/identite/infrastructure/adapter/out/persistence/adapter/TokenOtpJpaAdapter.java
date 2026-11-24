package com.njangi.identite.infrastructure.adapter.out.persistence.adapter;

import com.njangi.identite.domain.model.TokenOtp;
import com.njangi.identite.domain.port.out.TokenOtpRepositoryPort;
import com.njangi.identite.infrastructure.adapter.out.persistence.entity.TokenOtpJpaEntity;
import com.njangi.identite.infrastructure.adapter.out.persistence.mapper.AuthJpaMapper;
import com.njangi.identite.infrastructure.adapter.out.persistence.repository.SpringDataTokenOtpRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TokenOtpJpaAdapter implements TokenOtpRepositoryPort {

    private final SpringDataTokenOtpRepository repository;

    public TokenOtpJpaAdapter(SpringDataTokenOtpRepository repository) {
        this.repository = repository;
    }

    @Override
    public TokenOtp sauvegarder(TokenOtp tokenOtp) {
        TokenOtpJpaEntity entity = AuthJpaMapper.toJpaEntity(tokenOtp);
        TokenOtpJpaEntity saved = repository.save(entity);
        return AuthJpaMapper.toDomain(saved);
    }

    @Override
    public Optional<TokenOtp> trouverDernierNonUtiliseParIdentifiant(String identifiant) {
        return repository.findTopByIdentifiantAndUtiliseFalseOrderByCreeLeDesc(identifiant)
                .map(AuthJpaMapper::toDomain);
    }
}
