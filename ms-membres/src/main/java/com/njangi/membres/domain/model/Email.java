package com.njangi.membres.domain.model;

import java.util.Objects;

public record Email(String value) {
    public Email {
        Objects.requireNonNull(value, "L'email est requis");
        String clean = value.trim().toLowerCase();
        if (!clean.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Format d'email invalide : " + value);
        }
        value = clean;
    }

    @Override
    public String toString() {
        return value;
    }
}
