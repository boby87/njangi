package com.njangi.cotisations.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PlanifierTourPotRequest(
    @NotNull(message = "L'ID du groupe est obligatoire")
    UUID groupeId,

    @NotNull(message = "L'ID de la session est obligatoire")
    UUID sessionTontineId,

    @NotEmpty(message = "La liste ordonnée des bénéficiaires est obligatoire")
    List<UUID> ordreBeneficiaires,

    @NotNull(message = "Le montant total de cagnotte par tour est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à zéro")
    BigDecimal montantParTour
) {
}
