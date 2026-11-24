package com.njangi.identite.application.service;

import com.njangi.identite.domain.event.RoleAssigneeEvent;
import com.njangi.identite.domain.exception.NotFoundException;
import com.njangi.identite.domain.model.AdhesionGroupe;
import com.njangi.identite.domain.model.RoleMembre;
import com.njangi.identite.domain.model.StatutMembre;
import com.njangi.identite.domain.port.out.AdhesionRepositoryPort;
import com.njangi.identite.domain.port.out.IdentiteEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GererAdhesionGroupeServiceTest {

    @Mock
    private AdhesionRepositoryPort adhesionRepositoryPort;
    @Mock
    private IdentiteEventPublisherPort eventPublisherPort;

    private GererAdhesionGroupeService service;

    @BeforeEach
    void setUp() {
        service = new GererAdhesionGroupeService(adhesionRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Crée une nouvelle adhésion au groupe avec le rôle demandé")
    void testAdhererAuGroupeNouvelleAdhesion() {
        UUID userId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();

        when(adhesionRepositoryPort.trouverParUtilisateurEtGroupe(userId, groupeId)).thenReturn(Optional.empty());
        when(adhesionRepositoryPort.sauvegarder(any(AdhesionGroupe.class))).thenAnswer(inv -> inv.getArgument(0));

        AdhesionGroupe adhesion = service.adhererAuGroupe(userId, groupeId, RoleMembre.CREATEUR);

        assertNotNull(adhesion.getId());
        assertEquals(userId, adhesion.getUtilisateurId());
        assertEquals(groupeId, adhesion.getGroupeId());
        assertEquals(RoleMembre.CREATEUR, adhesion.getRole());
        assertEquals(StatutMembre.ACTIF, adhesion.getStatut());
        verify(adhesionRepositoryPort).sauvegarder(any(AdhesionGroupe.class));
    }

    @Test
    @DisplayName("Retourne l'adhésion existante de manière idempotente si le membre appartient déjà au groupe")
    void testAdhererAuGroupeIdempotent() {
        UUID userId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        AdhesionGroupe existante = new AdhesionGroupe(
                UUID.randomUUID(), userId, groupeId, RoleMembre.PRESIDENT, StatutMembre.ACTIF,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(adhesionRepositoryPort.trouverParUtilisateurEtGroupe(userId, groupeId)).thenReturn(Optional.of(existante));

        AdhesionGroupe result = service.adhererAuGroupe(userId, groupeId, RoleMembre.MEMBRE);

        assertEquals(existante.getId(), result.getId());
        assertEquals(RoleMembre.PRESIDENT, result.getRole());
        verify(adhesionRepositoryPort, never()).sauvegarder(any());
    }

    @Test
    @DisplayName("Change le rôle d'un membre dans un groupe et publie l'événement ROLE_ASSIGNE")
    void testChangerRoleSucces() {
        UUID adhesionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        AdhesionGroupe existante = new AdhesionGroupe(
                adhesionId, userId, groupeId, RoleMembre.CREATEUR, StatutMembre.ACTIF,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(adhesionRepositoryPort.trouverParId(adhesionId)).thenReturn(Optional.of(existante));
        when(adhesionRepositoryPort.sauvegarder(any(AdhesionGroupe.class))).thenAnswer(inv -> inv.getArgument(0));

        AdhesionGroupe maj = service.changerRole(adhesionId, RoleMembre.MEMBRE);

        assertEquals(RoleMembre.MEMBRE, maj.getRole());

        ArgumentCaptor<RoleAssigneeEvent> captor = ArgumentCaptor.forClass(RoleAssigneeEvent.class);
        verify(eventPublisherPort).publishRoleAssigne(captor.capture());
        assertEquals(adhesionId, captor.getValue().adhesionId());
        assertEquals(userId, captor.getValue().utilisateurId());
        assertEquals(groupeId, captor.getValue().groupeId());
        assertEquals(RoleMembre.MEMBRE, captor.getValue().nouveauRole());
    }

    @Test
    @DisplayName("Lève NotFoundException si l'adhésion est introuvable lors d'un changement de rôle")
    void testChangerRoleAdhesionIntrouvable() {
        UUID adhesionId = UUID.randomUUID();
        when(adhesionRepositoryPort.trouverParId(adhesionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.changerRole(adhesionId, RoleMembre.TRESORIER));
        verify(eventPublisherPort, never()).publishRoleAssigne(any());
    }

    @Test
    @DisplayName("Liste les adhésions par groupe et par utilisateur")
    void testListerAdhesions() {
        UUID userId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();
        AdhesionGroupe a = new AdhesionGroupe(
                UUID.randomUUID(), userId, groupeId, RoleMembre.SECRETAIRE, StatutMembre.ACTIF,
                LocalDateTime.now(), LocalDateTime.now()
        );

        when(adhesionRepositoryPort.trouverParGroupeId(groupeId)).thenReturn(List.of(a));
        when(adhesionRepositoryPort.trouverParUtilisateurId(userId)).thenReturn(List.of(a));

        assertEquals(1, service.listerMembresGroupe(groupeId).size());
        assertEquals(1, service.listerGroupesUtilisateur(userId).size());
    }
}
