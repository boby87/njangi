package com.njangi.reunions.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrdreJourRequest(
        @NotBlank(message = "L'ordre du jour ne peut pas être vide")
        String ordreJour
) {}
