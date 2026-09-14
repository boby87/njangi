package com.njangi.paiements.controller;

import com.njangi.paiements.dto.*;
import com.njangi.paiements.service.PaiementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paiements")
@Tag(name = "Paiements & Caisse", description = "Règlements Cash avec preuve obligatoire, paiements Mobile Money MTN/Orange et webhooks idempotents")
public class PaiementController {

    private final PaiementService paiementService;

    public PaiementController(PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @PostMapping("/cash")
    @Operation(summary = "Initier un règlement en espèces (Cash) avec preuve (reçu signé) obligatoire")
    public ResponseEntity<ApiResponse<PaiementDto>> initierPaiementCash(
            @Valid @RequestBody InitierPaiementCashRequest request) {
        PaiementDto dto = paiementService.initierPaiementCash(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Paiement en espèces soumis, en attente de validation du trésorier"));
    }

    @PutMapping("/{id}/valider-cash")
    @Operation(summary = "Valider ou rejeter un paiement en espèces (action réservée au trésorier)")
    public ResponseEntity<ApiResponse<PaiementDto>> validerPaiementCash(
            @PathVariable UUID id,
            @Valid @RequestBody ValiderPaiementCashRequest request) {
        PaiementDto dto = paiementService.validerPaiementCash(id, request);
        String message = request.valide() ? "Paiement Cash validé avec succès" : "Paiement Cash rejeté";
        return ResponseEntity.ok(ApiResponse.success(dto, message));
    }

    @PostMapping("/mobile-money")
    @Operation(summary = "Initier un paiement Mobile Money (MTN MoMo ou Orange Money) avec clé d'idempotence")
    public ResponseEntity<ApiResponse<PaiementDto>> initierPaiementMobileMoney(
            @Valid @RequestBody InitierPaiementMobileMoneyRequest request) {
        PaiementDto dto = paiementService.initierPaiementMobileMoney(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Demande de paiement Mobile Money initiée avec succès"));
    }

    @PostMapping("/webhooks/{operateur}")
    @Operation(summary = "Webhook de notification idempotent pour MTN MoMo et Orange Money")
    public ResponseEntity<ApiResponse<PaiementDto>> traiterWebhook(
            @PathVariable String operateur,
            @Valid @RequestBody WebhookPaiementRequest request) {
        PaiementDto dto = paiementService.traiterWebhook(operateur, request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Notification webhook traitée avec succès"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un paiement par ID")
    public ResponseEntity<ApiResponse<PaiementDto>> obtenirParId(@PathVariable UUID id) {
        PaiementDto dto = paiementService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @GetMapping("/cotisation/{cotisationId}")
    @Operation(summary = "Lister les paiements associés à une cotisation")
    public ResponseEntity<ApiResponse<List<PaiementDto>>> obtenirParCotisation(@PathVariable UUID cotisationId) {
        List<PaiementDto> dtos = paiementService.obtenirParCotisation(cotisationId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister tous les paiements d'un groupe")
    public ResponseEntity<ApiResponse<List<PaiementDto>>> obtenirParGroupe(@PathVariable UUID groupeId) {
        List<PaiementDto> dtos = paiementService.obtenirParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/membre/{membreId}")
    @Operation(summary = "Lister tous les paiements effectués par un membre")
    public ResponseEntity<ApiResponse<List<PaiementDto>>> obtenirParMembre(@PathVariable UUID membreId) {
        List<PaiementDto> dtos = paiementService.obtenirParMembre(membreId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }
}
