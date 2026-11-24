package com.njangi.gouvernance.dto;

import com.njangi.gouvernance.entity.RoleMembre;
import com.njangi.gouvernance.entity.StatutMembreGroupe;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupeMembreDto(
        UUID id,
        UUID groupeId,
        UUID membreId,
        RoleMembre role,
        StatutMembreGroupe statut,
        LocalDateTime dateAdhesion,
        LocalDateTime dateFin
) {}
