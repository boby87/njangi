package com.njangi.gouvernance.service;

import com.njangi.gouvernance.dto.AjouterMembreRequest;
import com.njangi.gouvernance.dto.CreerGroupeRequest;
import com.njangi.gouvernance.dto.GroupeDto;
import com.njangi.gouvernance.dto.GroupeMembreDto;
import com.njangi.gouvernance.entity.*;
import com.njangi.gouvernance.event.GouvernanceEventPublisher;
import com.njangi.gouvernance.exception.BusinessException;
import com.njangi.gouvernance.exception.NotFoundException;
import com.njangi.gouvernance.repository.GroupeMembreRepository;
import com.njangi.gouvernance.repository.GroupeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupeServiceTest {

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeMembreRepository groupeMembreRepository;

    @Mock
    private GouvernanceEventPublisher eventPublisher;

    private GroupeService groupeService;

    @BeforeEach
    void setUp() {
        groupeService = new GroupeService(groupeRepository, groupeMembreRepository, eventPublisher);
    }

    @Test
    @DisplayName("Création d'un groupe avec assignation automatique du créateur comme premier membre")
    void testCreerGroupe() {
        UUID createurId = UUID.randomUUID();
        CreerGroupeRequest request = new CreerGroupeRequest(
                "Solidarité Akwa",
                "Tontine d'entraide",
                createurId,
                TypeSiege.FIXE,
                "Douala Akwa",
                BigDecimal.valueOf(50000),
                "MENSUELLE",
                20,
                "Cotisation obligatoire le premier samedi"
        );

        when(groupeRepository.save(any(Groupe.class))).thenAnswer(inv -> {
            Groupe g = inv.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });

        GroupeDto dto = groupeService.create(request);

        assertNotNull(dto);
        assertNotNull(dto.id());
        assertEquals("Solidarité Akwa", dto.nom());
        assertNotNull(dto.codeInvitation());
        assertTrue(dto.codeInvitation().startsWith("NJG-"));

        verify(groupeRepository, times(1)).save(any(Groupe.class));
        verify(groupeMembreRepository, times(1)).save(argThat(gm ->
                gm.getRole() == RoleMembre.CREATEUR && gm.getMembreId().equals(createurId)
        ));
        verify(eventPublisher, times(1)).publierGroupeCree(any(), eq("Solidarité Akwa"), eq(createurId), eq("FIXE"));
    }

    @Test
    @DisplayName("Ajout d'un membre avec succès")
    void testAjouterMembre() {
        UUID groupeId = UUID.randomUUID();
        UUID membreId = UUID.randomUUID();

        Groupe groupe = new Groupe();
        groupe.setId(groupeId);
        groupe.setStatut(StatutGroupe.ACTIF);
        groupe.setNombreMembresMax(15);

        when(groupeRepository.findById(groupeId)).thenReturn(Optional.of(groupe));
        when(groupeMembreRepository.existsByGroupeIdAndMembreId(groupeId, membreId)).thenReturn(false);
        when(groupeMembreRepository.countByGroupeIdAndStatut(groupeId, StatutMembreGroupe.ACTIF)).thenReturn(5L);
        when(groupeMembreRepository.save(any(GroupeMembre.class))).thenAnswer(inv -> {
            GroupeMembre gm = inv.getArgument(0);
            gm.setId(UUID.randomUUID());
            return gm;
        });

        GroupeMembreDto dto = groupeService.ajouterMembre(groupeId, new AjouterMembreRequest(membreId, RoleMembre.MEMBRE));

        assertNotNull(dto);
        assertEquals(RoleMembre.MEMBRE, dto.role());
        verify(eventPublisher, times(1)).publierMembreAjoute(eq(groupeId), eq(membreId), eq("MEMBRE"));
    }

    @Test
    @DisplayName("Refus d'ajouter un membre lorsque le quota maximum est atteint")
    void testAjouterMembreQuotaDepasse() {
        UUID groupeId = UUID.randomUUID();
        UUID membreId = UUID.randomUUID();

        Groupe groupe = new Groupe();
        groupe.setId(groupeId);
        groupe.setStatut(StatutGroupe.ACTIF);
        groupe.setNombreMembresMax(5);

        when(groupeRepository.findById(groupeId)).thenReturn(Optional.of(groupe));
        when(groupeMembreRepository.existsByGroupeIdAndMembreId(groupeId, membreId)).thenReturn(false);
        when(groupeMembreRepository.countByGroupeIdAndStatut(groupeId, StatutMembreGroupe.ACTIF)).thenReturn(5L);

        assertThrows(BusinessException.class, () ->
                groupeService.ajouterMembre(groupeId, new AjouterMembreRequest(membreId, RoleMembre.MEMBRE))
        );
    }

    @Test
    @DisplayName("Recherche par ID inexistant lève NotFoundException")
    void testFindByIdInexistant() {
        UUID id = UUID.randomUUID();
        when(groupeRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> groupeService.findById(id));
    }

    @Test
    @DisplayName("Un nouvel utilisateur sans adhésion ne reçoit aucune tontine (isolation multi-tenant)")
    void testFindByMembreVidePourNouvelUtilisateur() {
        UUID nouveauMembreId = UUID.randomUUID();
        when(groupeMembreRepository.findByMembreId(nouveauMembreId)).thenReturn(List.of());
        when(groupeRepository.findByCreateurMembreId(nouveauMembreId)).thenReturn(List.of());

        List<GroupeDto> result = groupeService.findByMembre(nouveauMembreId);

        assertTrue(result.isEmpty());
        verify(groupeRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("findByMembre retourne uniquement les tontines actives du membre sans doublons")
    void testFindByMembreIsolation() {
        UUID membreId = UUID.randomUUID();
        UUID groupeId = UUID.randomUUID();

        GroupeMembre gm = new GroupeMembre();
        gm.setGroupeId(groupeId);
        gm.setMembreId(membreId);
        gm.setStatut(StatutMembreGroupe.ACTIF);

        Groupe groupe = new Groupe();
        groupe.setId(groupeId);
        groupe.setNom("Ma Tontine Privée");
        groupe.setCreateurMembreId(membreId);
        groupe.setTypeSiege(TypeSiege.FIXE);
        groupe.setStatut(StatutGroupe.ACTIF);

        when(groupeMembreRepository.findByMembreId(membreId)).thenReturn(List.of(gm));
        when(groupeRepository.findByCreateurMembreId(membreId)).thenReturn(List.of(groupe));
        when(groupeRepository.findAllById(any())).thenReturn(List.of(groupe));

        List<GroupeDto> result = groupeService.findByMembre(membreId);

        assertEquals(1, result.size());
        assertEquals("Ma Tontine Privée", result.getFirst().nom());
    }
}
