package com.njangi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Requête d'inscription d'un nouvel utilisateur")
public record InscriptionRequest(
        @Schema(description = "Numéro de téléphone au format international E.164 (ex: +237699123456 ou +33612345678) ou local", example = "+237699123456")
        @NotBlank(message = "Le numéro de téléphone est obligatoire")
        @Pattern(regexp = "^(\\+[1-9][0-9]{6,14}|[236][0-9]{8})$", message = "Format de numéro de téléphone invalide (attendu: E.164 ex: +237699123456 ou +33612345678)")
        String telephone,

        @Schema(description = "Adresse email de l'utilisateur", example = "jp.mbarga@njangi.cm")
        @Email(message = "Format d'email invalide")
        String email,

        @Schema(description = "Mot de passe", example = "Secret123!")
        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
        String motDePasse,

        @Schema(description = "Nom de famille", example = "Mbarga")
        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @Schema(description = "Prénom", example = "Jean-Paul")
        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @Schema(description = "URL de la photo de profil", example = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d")
        String photoUrl,

        @Schema(description = "Ville de résidence", example = "Douala")
        String ville,

        @Schema(description = "Pays de résidence", example = "Cameroun")
        String pays
) {}
