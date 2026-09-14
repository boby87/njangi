package com.njangi.membres.domain.port.out;

import com.njangi.membres.domain.event.MembreInscritEvent;
import com.njangi.membres.domain.event.RoleAssigneeEvent;

public interface MembreEventPublisherPort {
    void publierMembreInscrit(MembreInscritEvent event);
    void publierRoleAssignee(RoleAssigneeEvent event);
}
