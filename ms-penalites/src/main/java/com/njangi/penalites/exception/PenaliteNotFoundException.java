package com.njangi.penalites.exception;

import java.util.UUID;

public class PenaliteNotFoundException extends RuntimeException {

    public PenaliteNotFoundException(UUID id) {
        super("Pénalité non trouvée avec l'identifiant : " + id);
    }

    public PenaliteNotFoundException(String message) {
        super(message);
    }
}
