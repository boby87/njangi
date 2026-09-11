package com.njangi.penalites.controller;

import com.njangi.penalites.dto.PenaliteDto;
import com.njangi.penalites.service.PenaliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/penalites")
@RequiredArgsConstructor
public class PenaliteController {

    private final PenaliteService penaliteService;

    @PostMapping
    public ResponseEntity<PenaliteDto> appliquer(@Valid @RequestBody PenaliteDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(penaliteService.appliquer(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenaliteDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(penaliteService.findById(id));
    }

    @PutMapping("/{id}/payer")
    public ResponseEntity<PenaliteDto> payer(@PathVariable UUID id) {
        return ResponseEntity.ok(penaliteService.payer(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<PenaliteDto> annuler(@PathVariable UUID id) {
        return ResponseEntity.ok(penaliteService.annuler(id));
    }

    @GetMapping("/membre/{membreId}")
    public ResponseEntity<List<PenaliteDto>> findByMembre(@PathVariable UUID membreId) {
        return ResponseEntity.ok(penaliteService.findByMembre(membreId));
    }

    @GetMapping("/groupe/{groupeId}")
    public ResponseEntity<List<PenaliteDto>> findByGroupe(@PathVariable UUID groupeId) {
        return ResponseEntity.ok(penaliteService.findByGroupe(groupeId));
    }
}
