package com.njangi.identite.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class TelephoneTest {

    @Test
    @DisplayName("Normalise un numéro camerounais local (9 chiffres commençant par 6) en +237")
    void testNumeroCamerounaisLocalMobile() {
        Telephone tel = new Telephone("699123456");
        assertEquals("+237699123456", tel.numero());
        assertTrue(tel.isCamerounais());
    }

    @Test
    @DisplayName("Normalise un numéro camerounais local avec espaces (fixe commençant par 2)")
    void testNumeroCamerounaisAvecEspaces() {
        Telephone tel = new Telephone("  233 42 56 78  ");
        assertEquals("+237233425678", tel.numero());
        assertTrue(tel.isCamerounais());
    }

    @ParameterizedTest
    @ValueSource(strings = {"+237699123456", "+33612345678", "+14155552671", "+447700900077"})
    @DisplayName("Accepte les numéros internationaux au format E.164 (Cameroun et Diaspora)")
    void testNumerosInternationauxValides(String raw) {
        Telephone tel = new Telephone(raw);
        assertEquals(raw, tel.numero());
    }

    @Test
    @DisplayName("Distingue un numéro de la diaspora d'un numéro camerounais")
    void testNumeroDiasporaNonCamerounais() {
        Telephone tel = new Telephone("+33612345678");
        assertFalse(tel.isCamerounais());
    }

    @Test
    @DisplayName("Rejette un numéro null")
    void testNumeroNullRejete() {
        assertThrows(NullPointerException.class, () -> new Telephone(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "12345", "0699123456", "+0123456789", "abc699123456", "+2371234567890123456"})
    @DisplayName("Rejette les formats de numéro invalides")
    void testNumerosInvalidesRejetes(String invalid) {
        assertThrows(IllegalArgumentException.class, () -> new Telephone(invalid));
    }
}
