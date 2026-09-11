package com.njangi.paiements.controller;

import com.njangi.paiements.dto.CreatePaiementRequest;
import com.njangi.paiements.dto.PaiementDto;
import com.njangi.paiements.dto.ValiderPaiementRequest;
import com.njangi.paiements.service.PaiementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paiements")
@RequiredArgsConstructor
public class PaiementController {

    private final PaiementService paiementService;

    @GetMapping
    public ResponseEntity<List<PaiementDto>> findAll() {
        return ResponseEntity.ok(paiementService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaiementDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(paiementService.findById(id));
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<PaiementDto>> findByMembre(@PathVariable UUID membreId) {
        return ResponseEntity.ok(paiementService.findByMembre(membreId));
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<PaiementDto>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(paiementService.findByGroupe(groupeId));
    }

    @PostMapping
    public ResponseEntity<PaiementDto> create(@Valid @RequestBody CreatePaiementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paiementService.create(request));
    }

    @PutMapping("/{id}/valider")
    public ResponseEntity<PaiementDto> valider(
            @PathVariable UUID id,
            @Valid @RequestBody ValiderPaiementRequest request) {
        return ResponseEntity.ok(paiementService.validerPaiement(id, request));
    }
}
