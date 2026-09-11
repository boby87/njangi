package com.njangi.paiements.exception;

import java.util.UUID;

public class PaiementNotFoundException extends RuntimeException {

    public PaiementNotFoundException(UUID id) {
        super("Paiement non trouvé avec l'identifiant : " + id);
    }

    public PaiementNotFoundException(String message) {
        super(message);
    }
}
