package com.njangi.membres.domain.event;

import com.njangi.membres.domain.model.RoleMembre;
import java.time.Instant;
import java.util.UUID;

public record RoleAssigneeEvent(
        UUID adhesionId,
        UUID utilisateurId,
        UUID groupeId,
        RoleMembre nouveauRole,
        Instant survenuLe
) {
    public RoleAssigneeEvent(UUID adhesionId, UUID utilisateurId, UUID groupeId, RoleMembre nouveauRole) {
        this(adhesionId, utilisateurId, groupeId, nouveauRole, Instant.now());
    }
}
