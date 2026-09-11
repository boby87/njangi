package com.njangi.penalites.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resource, Object id) {
        super(resource + " introuvable avec l'identifiant : " + id);
    }
}
