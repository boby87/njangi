package com.njangi.identite.infrastructure.adapter.out.persistence.mapper;

import com.njangi.identite.domain.model.TokenOtp;
import com.njangi.identite.domain.model.Utilisateur;
import com.njangi.identite.infrastructure.adapter.out.persistence.entity.TokenOtpJpaEntity;
import com.njangi.identite.infrastructure.adapter.out.persistence.entity.UtilisateurJpaEntity;

public final class AuthJpaMapper {

    private AuthJpaMapper() {
    }

    public static Utilisateur toDomain(UtilisateurJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Utilisateur(
                entity.getId(),
                entity.getTelephone(),
                entity.getEmail(),
                entity.getMotDePasseHash(),
                entity.getNom(),
                entity.getPrenom(),
                entity.getPhotoUrl(),
                entity.getVille(),
                entity.getPays(),
                entity.getStatut(),
                entity.getProviderAuth(),
                entity.getProviderId(),
                entity.getCreeLe(),
                entity.getModifieLe()
        );
    }

    public static UtilisateurJpaEntity toJpaEntity(Utilisateur domain) {
        if (domain == null) {
            return null;
        }
        UtilisateurJpaEntity entity = new UtilisateurJpaEntity();
        entity.setId(domain.getId());
        entity.setTelephone(domain.getTelephone());
        entity.setEmail(domain.getEmail());
        entity.setMotDePasseHash(domain.getMotDePasseHash());
        entity.setNom(domain.getNom());
        entity.setPrenom(domain.getPrenom());
        entity.setPhotoUrl(domain.getPhotoUrl());
        entity.setVille(domain.getVille());
        entity.setPays(domain.getPays());
        entity.setStatut(domain.getStatut());
        entity.setProviderAuth(domain.getProviderAuth());
        entity.setProviderId(domain.getProviderId());
        entity.setCreeLe(domain.getCreeLe());
        entity.setModifieLe(domain.getModifieLe());
        return entity;
    }

    public static TokenOtp toDomain(TokenOtpJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return new TokenOtp(
                entity.getId(),
                entity.getIdentifiant(),
                entity.getCode(),
                entity.getExpireLe(),
                entity.isUtilise(),
                entity.getCreeLe()
        );
    }

    public static TokenOtpJpaEntity toJpaEntity(TokenOtp domain) {
        if (domain == null) {
            return null;
        }
        TokenOtpJpaEntity entity = new TokenOtpJpaEntity();
        entity.setId(domain.getId());
        entity.setIdentifiant(domain.getIdentifiant());
        entity.setCode(domain.getCode());
        entity.setExpireLe(domain.getExpireLe());
        entity.setUtilise(domain.isUtilise());
        entity.setCreeLe(domain.getCreeLe());
        return entity;
    }
}
