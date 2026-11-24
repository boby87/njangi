package com.njangi.identite.domain.model;

import java.util.Objects;

public record Telephone(String numero) {

    private static final String CAMEROUN_LOCAL_REGEX = "^[236][0-9]{8}$";
    private static final String E164_REGEX = "^\\+[1-9][0-9]{6,14}$";

    public Telephone {
        Objects.requireNonNull(numero, "Le numéro de téléphone est requis");
        String clean = numero.trim().replaceAll("\\s+", "");
        if (clean.matches(CAMEROUN_LOCAL_REGEX)) {
            numero = "+237" + clean;
        } else if (clean.matches(E164_REGEX)) {
            numero = clean;
        } else {
            throw new IllegalArgumentException(
                    "Numéro de téléphone invalide (format international E.164 attendu ex: +2376XXXXXXXX ou +336XXXXXXXX) : " + numero
            );
        }
    }

    public boolean isCamerounais() {
        return numero.startsWith("+237");
    }

    public static boolean isTelephonePattern(String identifiant) {
        if (identifiant == null) {
            return false;
        }
        String clean = identifiant.trim().replaceAll("\\s+", "");
        return clean.matches(CAMEROUN_LOCAL_REGEX) || clean.matches(E164_REGEX);
    }

    @Override
    public String toString() {
        return numero;
    }
}
