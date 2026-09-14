package com.njangi.groupes.service;

import com.njangi.groupes.dto.ElireBureauRequest;
import com.njangi.groupes.dto.MandatBureauDto;
import com.njangi.groupes.entity.*;
import com.njangi.groupes.event.GroupeEventPublisher;
import com.njangi.groupes.repository.GroupeMembreRepository;
import com.njangi.groupes.repository.GroupeRepository;
import com.njangi.groupes.repository.MandatBureauRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MandatBureauServiceTest {

    @Mock
    private MandatBureauRepository mandatRepository;

    @Mock
    private GroupeRepository groupeRepository;

    @Mock
    private GroupeMembreRepository groupeMembreRepository;

    @Mock
    private GroupeEventPublisher eventPublisher;

    private MandatBureauService mandatService;

    @BeforeEach
    void setUp() {
        mandatService = new MandatBureauService(mandatRepository, groupeRepository, groupeMembreRepository, eventPublisher);
    }

    @Test
    @DisplayName("Élection du bureau et rétrogradation automatique du créateur en membre simple (GEMINI.md)")
    void testElireBureauEtRetrograderCreateur() {
        UUID groupeId = UUID.randomUUID();
        UUID createurId = UUID.randomUUID();
        UUID presidentId = UUID.randomUUID();
        UUID tresorierId = UUID.randomUUID();
        UUID secretaireId = UUID.randomUUID();

        Groupe groupe = new Groupe();
        groupe.setId(groupeId);
        groupe.setCreateurMembreId(createurId);

        GroupeMembre adhesionCreateur = new GroupeMembre();
        adhesionCreateur.setGroupeId(groupeId);
        adhesionCreateur.setMembreId(createurId);
        adhesionCreateur.setRole(RoleMembre.CREATEUR);

        when(groupeRepository.findById(groupeId)).thenReturn(Optional.of(groupe));
        when(mandatRepository.findByGroupeIdAndActifTrue(groupeId)).thenReturn(Optional.empty());
        when(mandatRepository.save(any(MandatBureau.class))).thenAnswer(inv -> {
            MandatBureau mb = inv.getArgument(0);
            mb.setId(UUID.randomUUID());
            return mb;
        });

        when(groupeMembreRepository.findByGroupeIdAndMembreId(eq(groupeId), any())).thenAnswer(inv -> {
            UUID mId = inv.getArgument(1);
            if (mId.equals(createurId)) {
                return Optional.of(adhesionCreateur);
            }
            return Optional.empty();
        });

        ElireBureauRequest request = new ElireBureauRequest(
                presidentId,
                tresorierId,
                secretaireId,
                null,
                null,
                LocalDate.now(),
                LocalDate.now().plusYears(1)
        );

        MandatBureauDto dto = mandatService.elireBureau(groupeId, request);

        assertNotNull(dto);
        assertEquals(presidentId, dto.presidentMembreId());
        assertEquals(tresorierId, dto.tresorierMembreId());
        assertEquals(secretaireId, dto.secretaireMembreId());
        assertTrue(dto.actif());

        // RÈGLE MÉTIER CRITIQUE : Le créateur a été rétrogradé en MEMBRE
        assertEquals(RoleMembre.MEMBRE, adhesionCreateur.getRole());
        verify(groupeMembreRepository, atLeastOnce()).save(adhesionCreateur);

        // Publication de l'événement Kafka avec l'ID du créateur rétrogradé
        verify(eventPublisher, times(1)).publierBureauElu(
                eq(groupeId), eq(presidentId), eq(tresorierId), eq(secretaireId), eq(createurId)
        );
    }

    @Test
    @DisplayName("Expiration du mandat précédent lors de l'élection d'un nouveau bureau")
    void testExpirationAncienMandat() {
        UUID groupeId = UUID.randomUUID();
        Groupe groupe = new Groupe();
        groupe.setId(groupeId);
        groupe.setCreateurMembreId(UUID.randomUUID());

        MandatBureau ancienMandat = new MandatBureau();
        ancienMandat.setId(UUID.randomUUID());
        ancienMandat.setGroupeId(groupeId);
        ancienMandat.setActif(true);
        ancienMandat.setStatut(StatutMandat.EN_COURS);

        when(groupeRepository.findById(groupeId)).thenReturn(Optional.of(groupe));
        when(mandatRepository.findByGroupeIdAndActifTrue(groupeId)).thenReturn(Optional.of(ancienMandat));
        when(mandatRepository.save(any(MandatBureau.class))).thenAnswer(inv -> inv.getArgument(0));

        ElireBureauRequest request = new ElireBureauRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                LocalDate.now(),
                null
        );

        mandatService.elireBureau(groupeId, request);

        // L'ancien mandat doit être expiré et inactif
        assertFalse(ancienMandat.getActif());
        assertEquals(StatutMandat.EXPIRE, ancienMandat.getStatut());
    }
}
