package com.njangi.identite.application.dto;

import com.njangi.identite.domain.model.RoleMembre;

import java.util.UUID;

public record AssignerRoleCommand(
        UUID utilisateurId,
        UUID groupeId,
        RoleMembre role
) {}
