package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Demande de liaison d'un numéro de téléphone à un compte social")
public record LierTelephoneRequest(
        @Schema(description = "Numéro de téléphone au format international E.164 ou local camerounais", example = "+237699123456")
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+[1-9][0-9]{6,14}|[236][0-9]{8})$", message = "Format de numéro de téléphone invalide (attendu: E.164 ex: +237699123456 ou +33612345678)")
        String telephone,

        @Schema(description = "Code OTP optionnel (si déjà demandé)", example = "123456")
        String codeOtp
) {}
