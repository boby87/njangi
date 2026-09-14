package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Demande de génération et d'envoi de code OTP")
public record DemandeOtpRequest(
        @Schema(description = "Numéro de téléphone (E.164) ou adresse email destinataire", example = "+237699123456")
        @NotBlank(message = "L'identifiant (téléphone ou email) est obligatoire")
        String identifiant
) {}
