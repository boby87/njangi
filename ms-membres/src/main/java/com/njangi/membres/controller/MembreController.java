package com.njangi.membres.controller;

import com.njangi.membres.dto.*;
import com.njangi.membres.service.MembreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/membres")
@RequiredArgsConstructor
public class MembreController {

    private final MembreService membreService;

    @GetMapping
    public ResponseEntity<List<MembreDto>> getAll() {
        return ResponseEntity.ok(membreService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembreDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(membreService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MembreDto> create(@Valid @RequestBody CreateMembreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(membreService.create(request));
    }
}
