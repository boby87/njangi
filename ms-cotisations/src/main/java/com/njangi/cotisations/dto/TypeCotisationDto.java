package com.njangi.cotisations.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TypeCotisationDto(
        UUID id,
        UUID groupeId,
        String libelle,
        String description,
        BigDecimal montant,
        Boolean estRotatif,
        Boolean estObligatoire,
        String statut
) {}
