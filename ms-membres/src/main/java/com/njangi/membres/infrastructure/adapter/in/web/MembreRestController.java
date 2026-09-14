package com.njangi.membres.infrastructure.adapter.in.web;

import com.njangi.membres.application.dto.AdhesionGroupeDto;
import com.njangi.membres.application.dto.MembreDto;
import com.njangi.membres.domain.model.AdhesionGroupe;
import com.njangi.membres.domain.model.Membre;
import com.njangi.membres.domain.model.MembreId;
import com.njangi.membres.domain.model.RoleMembre;
import com.njangi.membres.domain.port.in.ConsulterMembreUseCase;
import com.njangi.membres.domain.port.in.GererAdhesionGroupeUseCase;
import com.njangi.membres.domain.port.in.InscrireMembreUseCase;
import com.njangi.membres.exception.NotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membres")
@Tag(name = "Membres & Rôles", description = "Endpoints pour l'inscription, la consultation des profils et la gestion des rôles dynamiques par groupe (Njangi)")
public class MembreRestController {

    private final InscrireMembreUseCase inscrireMembreUseCase;
    private final ConsulterMembreUseCase consulterMembreUseCase;
    private final GererAdhesionGroupeUseCase gererAdhesionGroupeUseCase;

    public MembreRestController(InscrireMembreUseCase inscrireMembreUseCase,
                                ConsulterMembreUseCase consulterMembreUseCase,
                                GererAdhesionGroupeUseCase gererAdhesionGroupeUseCase) {
        this.inscrireMembreUseCase = inscrireMembreUseCase;
        this.consulterMembreUseCase = consulterMembreUseCase;
        this.gererAdhesionGroupeUseCase = gererAdhesionGroupeUseCase;
    }

    public record InscrireMembreHttpRequest(
            @NotNull @Schema(description = "ID unique du compte Auth SSO/OTP", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID authUtilisateurId,

            @NotBlank @Schema(description = "Nom de famille", example = "FOKOU")
            String nom,

            @NotBlank @Schema(description = "Prénom", example = "Jean-Paul")
            String prenom,

            @NotBlank @Schema(description = "Adresse email valide et unique", example = "jp.fokou@njangi.cm")
            String email,

            @NotBlank @Schema(description = "Numéro de téléphone camerounais (+2376XXXXXXXX)", example = "+237699123456")
            String telephone,

            @Schema(description = "Date de naissance", example = "1990-05-15")
            LocalDate dateNaissance,

            @Schema(description = "Adresse de résidence", example = "Quartier Bastos")
            String adresse,

            @Schema(description = "Ville camerounaise", example = "Yaoundé")
            String ville
    ) {}

    public record AssignerRoleHttpRequest(
            @NotNull @Schema(description = "ID de l'adhésion au groupe", example = "123e4567-e89b-12d3-a456-426614174000")
            UUID adhesionId,

            @NotNull @Schema(description = "Nouveau rôle attribué dans le groupe", example = "TRESORIER")
            RoleMembre role
    ) {}

    public record AdhererGroupeHttpRequest(
            @NotNull @Schema(description = "ID de l'utilisateur", example = "550e8400-e29b-41d4-a716-446655440000")
            UUID utilisateurId,

            @NotNull @Schema(description = "ID du groupe de tontine", example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8")
            UUID groupeId,

            @Schema(description = "Rôle initial dans le groupe", example = "MEMBRE")
            RoleMembre role
    ) {}

    @Operation(summary = "Lister tous les membres", description = "Retourne l'annuaire complet des membres inscrits sur la plateforme.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès")
    })
    @GetMapping
    public ResponseEntity<List<MembreDto>> getAll() {
        List<MembreDto> membres = consulterMembreUseCase.listerTous().stream()
                .map(MembreDto::fromDomain)
                .toList();
        return ResponseEntity.ok(membres);
    }

    @Operation(summary = "Consulter un membre par ID", description = "Retourne le profil détaillé d'un membre avec ses adhésions de groupe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil trouvé"),
            @ApiResponse(responseCode = "404", description = "Membre introuvable", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MembreDto> getById(
            @Parameter(description = "UUID du membre", required = true) @PathVariable UUID id) {
        return consulterMembreUseCase.trouverParId(new MembreId(id))
                .map(m -> ResponseEntity.ok(MembreDto.fromDomain(m)))
                .orElseThrow(() -> new NotFoundException("Membre introuvable avec l'identifiant : " + id));
    }

    @Operation(summary = "Consulter un membre par son compte Auth", description = "Recherche un membre à partir de son identifiant d'authentification SSO/OTP.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil trouvé"),
            @ApiResponse(responseCode = "404", description = "Membre introuvable pour ce compte", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/auth/{authUtilisateurId}")
    public ResponseEntity<MembreDto> getByAuthId(
            @Parameter(description = "UUID du compte Auth", required = true) @PathVariable UUID authUtilisateurId) {
        return consulterMembreUseCase.trouverParAuthId(authUtilisateurId)
                .map(m -> ResponseEntity.ok(MembreDto.fromDomain(m)))
                .orElseThrow(() -> new NotFoundException("Membre introuvable pour le compte utilisateur : " + authUtilisateurId));
    }

    @Operation(summary = "Inscrire un nouveau membre", description = "Crée un membre en validant l'unicité du téléphone camerounais (+237...) et de l'email.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Membre créé et événement Kafka publié"),
            @ApiResponse(responseCode = "400", description = "Validation échouée (format invalide)", content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "Règle métier enfreinte (doublon email ou téléphone)", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<MembreDto> create(@Valid @RequestBody InscrireMembreHttpRequest request) {
        Membre nouveau = inscrireMembreUseCase.inscrire(
                request.authUtilisateurId(),
                request.nom(),
                request.prenom(),
                request.email(),
                request.telephone(),
                request.dateNaissance(),
                request.adresse(),
                request.ville()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(MembreDto.fromDomain(nouveau));
    }

    @Operation(summary = "Adhérer à un groupe", description = "Inscrit un membre dans un groupe de tontine avec un rôle spécifique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Adhésion enregistrée")
    })
    @PostMapping("/adhesions")
    public ResponseEntity<AdhesionGroupeDto> adherer(@Valid @RequestBody AdhererGroupeHttpRequest request) {
        AdhesionGroupe adhesion = gererAdhesionGroupeUseCase.adhererAuGroupe(
                request.utilisateurId(),
                request.groupeId(),
                request.role() != null ? request.role() : RoleMembre.MEMBRE
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(AdhesionGroupeDto.fromDomain(adhesion));
    }

    @Operation(summary = "Modifier le rôle dans un groupe", description = "Change le rôle d'un membre (Président, Trésorier, Secrétaire, Membre, Auditeur) et émet un événement Kafka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rôle mis à jour et diffusé via Kafka"),
            @ApiResponse(responseCode = "404", description = "Adhésion introuvable", content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/adhesions/role")
    public ResponseEntity<AdhesionGroupeDto> changerRole(@Valid @RequestBody AssignerRoleHttpRequest request) {
        AdhesionGroupe maj = gererAdhesionGroupeUseCase.changerRole(request.adhesionId(), request.role());
        return ResponseEntity.ok(AdhesionGroupeDto.fromDomain(maj));
    }

    @Operation(summary = "Lister les membres d'un groupe", description = "Retourne la liste des adhésions et rôles associés à un groupe de tontine.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des adhésions du groupe")
    })
    @GetMapping("/groupes/{groupeId}/adhesions")
    public ResponseEntity<List<AdhesionGroupeDto>> getAdhesionsGroupe(
            @Parameter(description = "UUID du groupe", required = true) @PathVariable UUID groupeId) {
        List<AdhesionGroupeDto> adhesions = gererAdhesionGroupeUseCase.listerMembresGroupe(groupeId).stream()
                .map(AdhesionGroupeDto::fromDomain)
                .toList();
        return ResponseEntity.ok(adhesions);
    }
}
