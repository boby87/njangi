package com.njangi.membres.infrastructure.adapter.out.persistence.mapper;

import com.njangi.membres.domain.model.*;
import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreGroupeJpaEntity;
import com.njangi.membres.infrastructure.adapter.out.persistence.entity.MembreJpaEntity;

import java.util.ArrayList;
import java.util.List;

public final class MembreJpaMapper {

    private MembreJpaMapper() {}

    public static Membre toDomain(MembreJpaEntity entity, List<AdhesionGroupe> adhesions) {
        if (entity == null) return null;
        return new Membre(
                new MembreId(entity.getId()),
                entity.getAuthUtilisateurId(),
                entity.getNom(),
                entity.getPrenom(),
                new Email(entity.getEmail()),
                new Telephone(entity.getTelephone()),
                entity.getDateNaissance(),
                entity.getAdresse(),
                entity.getVille(),
                entity.getPhotoUrl(),
                entity.getStatut(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                adhesions != null ? adhesions : new ArrayList<>()
        );
    }

    public static MembreJpaEntity toJpaEntity(Membre domain) {
        if (domain == null) return null;
        return new MembreJpaEntity(
                domain.getId().value(),
                domain.getAuthUtilisateurId(),
                domain.getNom(),
                domain.getPrenom(),
                domain.getEmail().value(),
                domain.getTelephone().numero(),
                domain.getDateNaissance(),
                domain.getAdresse(),
                domain.getVille(),
                domain.getPhotoUrl(),
                domain.getStatut(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }

    public static AdhesionGroupe toDomain(MembreGroupeJpaEntity entity) {
        if (entity == null) return null;
        return new AdhesionGroupe(
                entity.getId(),
                entity.getUtilisateurId(),
                entity.getGroupeId(),
                entity.getRole(),
                entity.getStatut(),
                entity.getRejointLe(),
                entity.getModifieLe()
        );
    }

    public static MembreGroupeJpaEntity toJpaEntity(AdhesionGroupe domain) {
        if (domain == null) return null;
        return new MembreGroupeJpaEntity(
                domain.getId(),
                domain.getUtilisateurId(),
                domain.getGroupeId(),
                domain.getRole(),
                domain.getStatut(),
                domain.getRejointLe(),
                domain.getModifieLe()
        );
    }
}
