package com.njangi.identite.domain.model;

import java.util.Objects;

public record Email(String value) {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$";

    public Email {
        Objects.requireNonNull(value, "L'email est requis");
        String clean = value.trim().toLowerCase();
        if (!clean.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Format d'email invalide : " + value);
        }
        value = clean;
    }

    @Override
    public String toString() {
        return value;
    }
}
