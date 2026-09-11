package com.njangi.groupes.dto;

import com.njangi.groupes.entity.RoleMembre;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AjouterMembreRequest(
    @NotNull UUID membreId,
    @NotNull RoleMembre role
) {}
