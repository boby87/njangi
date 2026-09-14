package com.njangi.cotisations.dto;

import com.njangi.cotisations.entity.CategorieCotisation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TypeCotisationDto(
    UUID id,
    UUID groupeId,
    String libelle,
    String description,
    CategorieCotisation categorie,
    BigDecimal montant,
    boolean estRotatif,
    boolean estObligatoire,
    String statut,
    LocalDateTime createdAt
) {
}
