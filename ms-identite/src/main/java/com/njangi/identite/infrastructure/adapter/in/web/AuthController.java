package com.njangi.identite.infrastructure.adapter.in.web;

import com.njangi.identite.application.dto.*;
import com.njangi.identite.application.service.AuthService;
import com.njangi.identite.application.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentification", description = "Endpoints d'inscription, connexion, OTP, authentification sociale et gestion des sessions Njangi")
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Compte créé avec succès et jeton JWT émis"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Données invalides ou téléphone/email déjà utilisé")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> inscrire(@Valid @RequestBody InscriptionRequest request) {
        LoginResponse response = authService.inscrire(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte Njangi créé avec succès", response));
    }

    @PostMapping({"/connexion", "/login"})
    @Operation(summary = "Connexion par mot de passe", description = "Authentification classique par identifiant (téléphone ou email) et mot de passe")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentification réussie"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Identifiant ou mot de passe incorrect")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> connexion(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.connexion(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    @PostMapping("/otp/demander")
    @Operation(summary = "Demander un code OTP", description = "Génère un code OTP à 6 chiffres valide 5 minutes et le diffuse via Kafka sur identite.events")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Code OTP généré et transmis"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Identifiant invalide")
    })
    public ResponseEntity<ApiResponse<Void>> demanderOtp(
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
        return ResponseEntity.ok(ApiResponse.success("Code OTP généré avec succès", null));
    }

    @PostMapping("/otp/verifier")
    @Operation(summary = "Valider un code OTP", description = "Vérifie le code OTP à 6 chiffres et authentifie l'utilisateur avec un jeton JWT")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OTP validé avec succès, jeton JWT délivré"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Code OTP incorrect ou expiré")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> verifierOtp(@Valid @RequestBody OtpVerifyRequest request) {
        LoginResponse response = authService.verifierOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Authentification par OTP réussie", response));
    }

    @GetMapping({"/moi", "/me"})
    @Operation(summary = "Consulter le profil de l'utilisateur connecté", description = "Récupère les informations de l'utilisateur connecté via l'en-tête X-User-Id ou le token Bearer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profil utilisateur trouvé"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Utilisateur introuvable")
    })
    public ResponseEntity<ApiResponse<UtilisateurDto>> moi(
            @RequestHeader(value = "X-User-Id", required = false) UUID headerUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UUID effectiveId = resolveUserId(headerUserId, authHeader);
        if (effectiveId == null) {
            throw new IllegalArgumentException("Identifiant utilisateur manquant (en-tête X-User-Id ou Authorization Bearer requis)");
        }

        UtilisateurDto dto = authService.moi(effectiveId);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PostMapping({"/valider-token", "/valider"})
    @Operation(summary = "Valider l'authenticité d'un jeton JWT", description = "Contrôle la validité et la signature d'un jeton JWT pour la Gateway ou les microservices tiers")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validerToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String token
    ) {
        String tokenToVerify = (token != null && !token.isBlank()) ? token : authHeader;
        TokenValidationResponse response = authService.validerToken(tokenToVerify);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping({"/social/connexion", "/social/login"})
    @Operation(summary = "Connexion ou inscription via Google ou Facebook", description = "Authentifie un utilisateur avec son compte Google ou Facebook et provisionne le compte automatiquement")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentification sociale réussie"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Fournisseur social invalide ou données manquantes")
    })
    public ResponseEntity<ApiResponse<SocialLoginResponse>> connexionSociale(
            @Valid @RequestBody SocialLoginRequest request
    ) {
        SocialLoginResponse response = authService.connexionSociale(request);
        return ResponseEntity.ok(ApiResponse.success("Connexion sociale réussie", response));
    }

    @PostMapping("/social/lier-telephone")
    @Operation(summary = "Lier un numéro de téléphone au compte social", description = "Associe un numéro de téléphone international ou camerounais au compte après connexion Google/Facebook")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Numéro de téléphone lié avec succès"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Numéro invalide, déjà pris ou code OTP requis")
    })
    public ResponseEntity<ApiResponse<LoginResponse>> lierTelephone(
            @RequestHeader(value = "X-User-Id", required = false) UUID headerUserId,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody LierTelephoneRequest request
    ) {
        UUID effectiveId = resolveUserId(headerUserId, authHeader);
        if (effectiveId == null) {
            throw new IllegalArgumentException("Identifiant utilisateur manquant (veuillez vous authentifier via Google/Facebook d'abord)");
        }

        LoginResponse response = authService.lierTelephone(effectiveId, request);
        return ResponseEntity.ok(ApiResponse.success("Numéro de téléphone lié avec succès", response));
    }

    private UUID resolveUserId(UUID headerUserId, String authHeader) {
        if (headerUserId != null) {
            return headerUserId;
        }
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return jwtService.extractUserId(authHeader.substring(7));
        }
        return null;
    }
}
