package com.njangi.membres.application.dto;

import com.njangi.membres.domain.model.RoleMembre;
import java.util.UUID;

public record AssignerRoleCommand(
        UUID utilisateurId,
        UUID groupeId,
        RoleMembre role
) {}
