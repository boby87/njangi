package com.njangi.membres.application.dto;

import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.StatutMembre;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MembreDto(
        UUID id,
        UUID authUtilisateurId,
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateNaissance,
        String adresse,
        String ville,
        String photoUrl,
        StatutMembre statut,
        LocalDateTime createdAt,
        List<AdhesionGroupeDto> adhesions
) {
    public static MembreDto fromDomain(Membre m) {
        List<AdhesionGroupeDto> adhesionsDto = m.getAdhesions().stream()
                .map(AdhesionGroupeDto::fromDomain)
                .toList();
        return new MembreDto(
                m.getId().value(),
                m.getAuthUtilisateurId(),
                m.getNom(),
                m.getPrenom(),
                m.getEmail().value(),
                m.getTelephone().numero(),
                m.getDateNaissance(),
                m.getAdresse(),
                m.getVille(),
                m.getPhotoUrl(),
                m.getStatut(),
                m.getCreatedAt(),
                adhesionsDto
        );
    }
}
