package com.njangi.groupes.dto;
import com.njangi.groupes.entity.Groupe;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record CreerGroupeRequest(
    @NotBlank String nom,
    String description,
    UUID createurId,
    @NotNull Groupe.FrequenceReunion frequence,
    @NotNull Groupe.TypeSiege typeSiege,
    String adresseSiege,
    @Min(2) @Max(100) Integer nombreMaxMembres,
    String langue
) {}
