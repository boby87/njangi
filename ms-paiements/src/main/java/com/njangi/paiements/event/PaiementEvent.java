package com.njangi.paiements.event;

import com.njangi.paiements.entity.ModePaiement;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaiementEvent(
    String type,
    UUID paiementId,
    UUID cotisationId,
    UUID membreId,
    UUID groupeId,
    BigDecimal montant,
    ModePaiement modePaiement,
    String reference,
    Instant timestamp,
    String details
) {
    public static PaiementEvent of(String type, UUID paiementId, UUID cotisationId, UUID membreId,
                                  UUID groupeId, BigDecimal montant, ModePaiement modePaiement,
                                  String reference, String details) {
        return new PaiementEvent(type, paiementId, cotisationId, membreId, groupeId, montant, modePaiement, reference, Instant.now(), details);
    }
}
