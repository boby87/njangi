package com.njangi.groupes.dto;

import com.njangi.groupes.entity.RoleMembre;
import com.njangi.groupes.entity.StatutMembreGroupe;
import java.time.LocalDateTime;
import java.util.UUID;

public record GroupeMembreDto(
    UUID id,
    UUID groupeId,
    UUID membreId,
    RoleMembre role,
    StatutMembreGroupe statut,
    LocalDateTime dateAdhesion
) {}
