package com.njangi.groupes.dto;

import com.njangi.groupes.entity.RoleMembre;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AjouterMembreRequest(
        @NotNull(message = "L'identifiant du membre est obligatoire")
        UUID membreId,
        RoleMembre role
) {}
