package com.njangi.identite.domain.port.out;

import com.njangi.identite.domain.event.MembreInscritEvent;
import com.njangi.identite.domain.event.RoleAssigneeEvent;

import java.util.UUID;

public interface IdentiteEventPublisherPort {
    void publishOtpDemande(String identifiant, String codeOtp);
    void publishUtilisateurInscrit(UUID utilisateurId, String identifiant, String nomComplet);
    void publishUtilisateurConnecte(UUID utilisateurId, String identifiant);
    void publishMembreInscrit(MembreInscritEvent event);
    void publishRoleAssigne(RoleAssigneeEvent event);
}
