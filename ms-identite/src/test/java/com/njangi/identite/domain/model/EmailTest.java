package com.njangi.identite.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {

    @Test
    @DisplayName("Normalise un email valide en minuscules et supprime les espaces")
    void testNormalisationEmail() {
        Email email = new Email("  Jean.Paul@Njangi.CM  ");
        assertEquals("jean.paul@njangi.cm", email.value());
        assertEquals("jean.paul@njangi.cm", email.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"membre@njangi.cm", "jp+tontine@sub.domain.org", "user_1.2-3@gmail.com"})
    @DisplayName("Accepte les adresses email valides")
    void testEmailsValides(String raw) {
        Email email = new Email(raw);
        assertEquals(raw.toLowerCase(), email.value());
    }

    @Test
    @DisplayName("Rejette un email null")
    void testEmailNullRejete() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "sans-arobase.cm", "@njangi.cm", "membre@", "membre@localhost", "membre@domain."})
    @DisplayName("Rejette les adresses email invalides")
    void testEmailsInvalidesRejetes(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Email(invalid));
    }
}
