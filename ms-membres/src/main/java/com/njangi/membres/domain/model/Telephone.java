package com.njangi.membres.domain.model;

import java.util.Objects;

public record Telephone(String numero) {
    public Telephone {
        Objects.requireNonNull(numero, "Le numéro de téléphone est requis");
        String clean = numero.trim().replaceAll("\\s+", "");
        if (clean.matches("^[236][0-9]{8}$")) {
            numero = "+237" + clean;
        } else if (clean.matches("^\\+[1-9][0-9]{6,14}$")) {
            numero = clean;
        } else {
            throw new IllegalArgumentException("Numéro de téléphone invalide (format international attendu ex: +2376XXXXXXXX ou +336XXXXXXXX) : " + numero);
        }
    }

    @Override
    public String toString() {
        return numero;
    }
}
