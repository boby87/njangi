package com.njangi.auth.controller;

import com.njangi.auth.dto.*;
import com.njangi.auth.service.AuthService;
import com.njangi.auth.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription, connexion, OTP et gestion des sessions Njangi")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/inscrire")
    @Operation(summary = "Inscrire un nouvel utilisateur", description = "Création de compte avec numéro de téléphone (international E.164 ou Cameroun) et mot de passe chiffré")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compte créé avec succès et jeton JWT émis"),
            @ApiResponse(responseCode = "400", description = "Données invalides ou téléphone/email déjà utilisé")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<LoginResponse>> inscrire(@Valid @RequestBody InscriptionRequest request) {
        LoginResponse response = authService.inscrire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.njangi.auth.dto.ApiResponse.success("Compte Njangi créé avec succès", response));
    }

    @PostMapping("/connexion")
    @Operation(summary = "Connexion par mot de passe", description = "Authentification classique par identifiant (téléphone ou email) et mot de passe")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @ApiResponse(responseCode = "400", description = "Identifiant ou mot de passe incorrect")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<LoginResponse>> connexion(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.connexion(request);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success("Connexion réussie", response));
    }

    @PostMapping("/otp/demander")
    @Operation(summary = "Demander un code OTP", description = "Génère un code OTP à 6 chiffres valide 5 minutes et le diffuse via Kafka pour envoi SMS/Push")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Code OTP généré et transmis"),
            @ApiResponse(responseCode = "400", description = "Identifiant invalide")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<Void>> demanderOtp(
            @Valid @RequestBody(required = false) DemandeOtpRequest bodyRequest,
            @RequestParam(required = false) String identifiant
    ) {
        String targetIdentifiant = (bodyRequest != null && bodyRequest.identifiant() != null)
                ? bodyRequest.identifiant()
                : identifiant;

        if (targetIdentifiant == null || targetIdentifiant.isBlank()) {
            throw new IllegalArgumentException("Le paramètre 'identifiant' (téléphone ou email) est obligatoire");
        }

        authService.demanderOtp(new DemandeOtpRequest(targetIdentifiant));
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success("Code OTP généré avec succès", null));
    }

    @PostMapping("/otp/verifier")
    @Operation(summary = "Valider un code OTP", description = "Vérifie le code OTP à 6 chiffres et authentifie l'utilisateur avec un jeton JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP validé avec succès, jeton JWT délivré"),
            @ApiResponse(responseCode = "400", description = "Code OTP incorrect ou expiré")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<LoginResponse>> verifierOtp(@Valid @RequestBody OtpVerifyRequest request) {
        LoginResponse response = authService.verifierOtp(request);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success("Authentification par OTP réussie", response));
    }

    @GetMapping("/moi")
    @Operation(summary = "Consulter le profil de l'utilisateur connecté", description = "Récupère les informations de l'utilisateur connecté via l'en-tête X-User-Id ou le token Bearer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil utilisateur trouvé"),
            @ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<UtilisateurDto>> moi(
            @RequestHeader(value = "X-User-Id", required = false) UUID headerUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UUID effectiveId = headerUserId;

        if (effectiveId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            effectiveId = jwtService.extractUserId(token);
        }

        if (effectiveId == null) {
            throw new IllegalArgumentException("Identifiant utilisateur manquant (en-tête X-User-Id ou Authorization Bearer requis)");
        }

        UtilisateurDto dto = authService.moi(effectiveId);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success(dto));
    }

    @PostMapping("/valider-token")
    @Operation(summary = "Valider l'authenticité d'un jeton JWT", description = "Contrôle la validité et la signature d'un jeton JWT pour la Gateway ou les microservices tiers")
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<TokenValidationResponse>> validerToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String token
    ) {
        String tokenToVerify = (token != null && !token.isBlank()) ? token : authHeader;
        TokenValidationResponse response = authService.validerToken(tokenToVerify);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success(response));
    }

    @PostMapping("/social/connexion")
    @Operation(summary = "Connexion ou inscription via Google ou Facebook", description = "Authentifie un utilisateur avec son compte Google ou Facebook et provisionne le compte automatiquement")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentification sociale réussie"),
            @ApiResponse(responseCode = "400", description = "Fournisseur social invalide ou données manquantes")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<SocialLoginResponse>> connexionSociale(
            @Valid @RequestBody SocialLoginRequest request
    ) {
        SocialLoginResponse response = authService.connexionSociale(request);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success("Connexion sociale réussie", response));
    }

    @PostMapping("/social/lier-telephone")
    @Operation(summary = "Lier un numéro de téléphone au compte social", description = "Associe un numéro de téléphone international ou camerounais au compte après connexion Google/Facebook")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Numéro de téléphone lié avec succès"),
            @ApiResponse(responseCode = "400", description = "Numéro invalide, déjà pris ou code OTP requis")
    })
    public ResponseEntity<com.njangi.auth.dto.ApiResponse<LoginResponse>> lierTelephone(
            @RequestHeader(value = "X-User-Id", required = false) UUID headerUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody LierTelephoneRequest request
    ) {
        UUID effectiveId = headerUserId;
        if (effectiveId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            effectiveId = jwtService.extractUserId(token);
        }

        if (effectiveId == null) {
            throw new IllegalArgumentException("Identifiant utilisateur manquant (veuillez vous authentifier via Google/Facebook d'abord)");
        }

        LoginResponse response = authService.lierTelephone(effectiveId, request);
        return ResponseEntity.ok(com.njangi.auth.dto.ApiResponse.success("Numéro de téléphone lié avec succès", response));
    }
}
