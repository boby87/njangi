package com.njangi.cotisations.dto;

import com.njangi.cotisations.entity.CategorieCotisation;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreerTypeCotisationRequest(
    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotBlank(message = "Le libellé est obligatoire")
    String libelle,

    String description,

    @NotNull(message = "La catégorie de cotisation est obligatoire")
    CategorieCotisation categorie,

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à zéro")
    BigDecimal montant,

    boolean estRotatif,

    boolean estObligatoire
) {
}
