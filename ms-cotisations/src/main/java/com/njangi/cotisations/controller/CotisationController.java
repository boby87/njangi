package com.njangi.cotisations.controller;

import com.njangi.cotisations.dto.*;
import com.njangi.cotisations.service.CotisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cotisations")
@RequiredArgsConstructor
public class CotisationController {

    private final CotisationService cotisationService;

    @GetMapping
    public ResponseEntity<List<CotisationDto>> findAll() {
        return ResponseEntity.ok(cotisationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CotisationDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(cotisationService.findById(id));
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<CotisationDto>> findByMembre(@PathVariable UUID membreId) {
        return ResponseEntity.ok(cotisationService.findByMembre(membreId));
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<CotisationDto>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(cotisationService.findByGroupe(groupeId));
    }

    @PostMapping
    public ResponseEntity<CotisationDto> create(@Valid @RequestBody CreateCotisationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.create(request));
    }

    @PostMapping("/{id}/payer")
    public ResponseEntity<CotisationDto> payer(@PathVariable UUID id) {
        return ResponseEntity.ok(cotisationService.marquerPayee(id));
    }

    @PostMapping("/pots/verser")
    public ResponseEntity<PotSessionDto> verserPot(@Valid @RequestBody VerserPotRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotisationService.verserPot(request));
    }
}
