package com.njangi.cotisations.service;

import com.njangi.cotisations.dto.CreerTypeCotisationRequest;
import com.njangi.cotisations.dto.TypeCotisationDto;
import com.njangi.cotisations.entity.CategorieCotisation;
import com.njangi.cotisations.entity.TypeCotisation;
import com.njangi.cotisations.repository.TypeCotisationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TypeCotisationServiceTest {

    @Mock
    private TypeCotisationRepository typeCotisationRepository;

    @InjectMocks
    private TypeCotisationService typeCotisationService;

    private UUID groupeId;

    @BeforeEach
    void setUp() {
        groupeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit créer un type de cotisation avec succès")
    void doitCreerTypeCotisation() {
        CreerTypeCotisationRequest request = new CreerTypeCotisationRequest(
                groupeId,
                "Cotisation Pot Rotatif",
                "Cotisation mensuelle obligatoire de 50 000 XAF pour le pot",
                CategorieCotisation.ROTATIVE_POT,
                BigDecimal.valueOf(50000),
                true,
                true
        );

        TypeCotisation entity = new TypeCotisation(
                UUID.randomUUID(),
                groupeId,
                request.libelle(),
                request.description(),
                request.categorie(),
                request.montant(),
                request.estRotatif(),
                request.estObligatoire(),
                "ACTIF"
        );

        when(typeCotisationRepository.save(any(TypeCotisation.class))).thenReturn(entity);

        TypeCotisationDto result = typeCotisationService.creerTypeCotisation(request);

        assertThat(result).isNotNull();
        assertThat(result.libelle()).isEqualTo("Cotisation Pot Rotatif");
        assertThat(result.montant()).isEqualByComparingTo(BigDecimal.valueOf(50000));
        assertThat(result.categorie()).isEqualTo(CategorieCotisation.ROTATIVE_POT);
        assertThat(result.estRotatif()).isTrue();
        assertThat(result.estObligatoire()).isTrue();
        assertThat(result.statut()).isEqualTo("ACTIF");

        verify(typeCotisationRepository).save(any(TypeCotisation.class));
    }

    @Test
    @DisplayName("Doit lister les types de cotisation actifs d'un groupe")
    void doitListerTypesCotisationActifs() {
        TypeCotisation t1 = new TypeCotisation(UUID.randomUUID(), groupeId, "Pot Rotatif", null,
                CategorieCotisation.ROTATIVE_POT, BigDecimal.valueOf(50000), true, true, "ACTIF");
        TypeCotisation t2 = new TypeCotisation(UUID.randomUUID(), groupeId, "Secours et Décès", null,
                CategorieCotisation.SECOURS_DECES, BigDecimal.valueOf(5000), false, true, "ACTIF");

        when(typeCotisationRepository.findByGroupeIdAndStatut(groupeId, "ACTIF")).thenReturn(List.of(t1, t2));

        List<TypeCotisationDto> result = typeCotisationService.obtenirTypesCotisationActifs(groupeId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).libelle()).isEqualTo("Pot Rotatif");
        assertThat(result.get(1).libelle()).isEqualTo("Secours et Décès");
    }

    @Test
    @DisplayName("Doit désactiver un type de cotisation")
    void doitDesactiverTypeCotisation() {
        UUID typeId = UUID.randomUUID();
        TypeCotisation entity = new TypeCotisation(typeId, groupeId, "Caisse Réserve", null,
                CategorieCotisation.CAISSE_RESERVE, BigDecimal.valueOf(10000), false, false, "ACTIF");

        when(typeCotisationRepository.findById(typeId)).thenReturn(Optional.of(entity));
        when(typeCotisationRepository.save(any(TypeCotisation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TypeCotisationDto result = typeCotisationService.desactiver(typeId);

        assertThat(result.statut()).isEqualTo("INACTIF");
        verify(typeCotisationRepository).save(entity);
    }
}
