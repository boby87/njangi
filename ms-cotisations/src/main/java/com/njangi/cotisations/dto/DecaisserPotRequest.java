package com.njangi.cotisations.dto;

import com.njangi.cotisations.entity.ModeVersement;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record DecaisserPotRequest(
    @NotNull(message = "Le mode de versement est obligatoire")
    ModeVersement modeVersement,

    String referencePaiement,

    BigDecimal montantNet
) {
}
