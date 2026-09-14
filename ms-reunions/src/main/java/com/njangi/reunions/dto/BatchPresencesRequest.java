package com.njangi.reunions.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchPresencesRequest(
        @NotEmpty(message = "La liste des présences ne peut pas être vide")
        List<@Valid EnregistrerPresenceRequest> presences
) {}
