package com.njangi.paiements.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resourceName, Object id) {
        super(resourceName + " introuvable avec l'identifiant : " + id);
    }
}
