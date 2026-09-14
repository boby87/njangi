package com.njangi.penalites.controller;

import com.njangi.penalites.dto.ApiResponse;
import com.njangi.penalites.dto.DefinirTarifRequest;
import com.njangi.penalites.dto.TarificationPenaliteDto;
import com.njangi.penalites.service.TarificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/penalites/tarifs")
@Tag(name = "Barème Disciplinaire", description = "Paramétrage de la tarification des infractions par groupe (retards, absences, bavardages)")
public class TarificationController {

    private final TarificationService tarificationService;

    public TarificationController(TarificationService tarificationService) {
        this.tarificationService = tarificationService;
    }

    @PostMapping
    @Operation(summary = "Définir ou mettre à jour un tarif d'infraction pour un groupe")
    public ResponseEntity<ApiResponse<TarificationPenaliteDto>> definirTarif(
            @Valid @RequestBody DefinirTarifRequest request) {
        TarificationPenaliteDto dto = tarificationService.definirOuMettreAJourTarif(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(dto, "Tarif d'infraction enregistré avec succès"));
    }

    @GetMapping("/groupe/{groupeId}")
    @Operation(summary = "Lister l'ensemble des tarifs d'infraction configurés pour un groupe")
    public ResponseEntity<ApiResponse<List<TarificationPenaliteDto>>> obtenirTarifsParGroupe(
            @PathVariable UUID groupeId) {
        List<TarificationPenaliteDto> dtos = tarificationService.obtenirTarifsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/groupe/{groupeId}/actifs")
    @Operation(summary = "Lister les tarifs d'infraction actifs d'un groupe")
    public ResponseEntity<ApiResponse<List<TarificationPenaliteDto>>> obtenirTarifsActifsParGroupe(
            @PathVariable UUID groupeId) {
        List<TarificationPenaliteDto> dtos = tarificationService.obtenirTarifsActifsParGroupe(groupeId);
        return ResponseEntity.ok(ApiResponse.success(dtos));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un tarif par son identifiant")
    public ResponseEntity<ApiResponse<TarificationPenaliteDto>> obtenirParId(@PathVariable UUID id) {
        TarificationPenaliteDto dto = tarificationService.obtenirParId(id);
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}
