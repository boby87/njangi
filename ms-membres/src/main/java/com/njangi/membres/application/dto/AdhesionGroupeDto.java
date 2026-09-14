package com.njangi.membres.application.dto;

import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.model.RoleMembre;
import com.njangi.membres.domain.model.StatutMembre;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdhesionGroupeDto(
        UUID id,
        UUID utilisateurId,
        UUID groupeId,
        RoleMembre role,
        StatutMembre statut,
        LocalDateTime rejointLe,
        LocalDateTime modifieLe
) {
    public static AdhesionGroupeDto fromDomain(AdhesionGroupe a) {
        return new AdhesionGroupeDto(
                a.getId(),
                a.getUtilisateurId(),
                a.getGroupeId(),
                a.getRole(),
                a.getStatut(),
                a.getRejointLe(),
                a.getModifieLe()
        );
    }
}
