package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Vérification du code OTP pour authentification")
public record OtpVerifyRequest(
        @Schema(description = "Identifiant (téléphone E.164 ou email)", example = "+237699123456")
        @NotBlank(message = "L'identifiant est obligatoire")
        String identifiant,

        @Schema(description = "Code OTP à 6 chiffres", example = "123456")
        @NotBlank(message = "Le code OTP est obligatoire")
        @Pattern(regexp = "^[0-9]{6}$", message = "Le code OTP doit être composé de 6 chiffres")
        String code
) {}
