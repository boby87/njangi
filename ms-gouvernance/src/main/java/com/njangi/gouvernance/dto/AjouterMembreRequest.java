package com.njangi.gouvernance.dto;

import com.njangi.gouvernance.entity.RoleMembre;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AjouterMembreRequest(
        @NotNull(message = "L'identifiant du membre est obligatoire")
        UUID membreId,
        RoleMembre role
) {}
