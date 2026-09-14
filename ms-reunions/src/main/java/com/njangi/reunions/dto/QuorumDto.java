package com.njangi.reunions.dto;

import java.util.UUID;

public record QuorumDto(
        UUID reunionId,
        long totalEnregistres,
        long presents,
        long retards,
        long excuses,
        long absents,
        double tauxPresencePourcent,
        boolean quorumAtteint
) {}
