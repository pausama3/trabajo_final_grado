package com.medmanager.escritorio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase Validaciones (formulario de alta y modificación de pacientes, CU-A03).
 */
class ValidacionesTest {

    @Test
    @DisplayName("Un DNI de 8 números y una letra es válido")
    void dniValido() {
        assertTrue(Validaciones.esDniONie("55555555E"));
        assertTrue(Validaciones.esDniONie("12345678z"));      // la letra puede ir en minúscula
        assertTrue(Validaciones.esDniONie("  12345678Z  "));  // se ignoran los espacios de los extremos
    }

    @Test
    @DisplayName("Un NIE (X, Y o Z, 7 números y una letra) es válido")
    void nieValido() {
        assertTrue(Validaciones.esDniONie("X1234567L"));
        assertTrue(Validaciones.esDniONie("Y7654321G"));
    }

    @Test
    @DisplayName("Un DNI con formato incorrecto se rechaza")
    void dniIncorrecto() {
        assertFalse(Validaciones.esDniONie("1234"));          // demasiado corto
        assertFalse(Validaciones.esDniONie("123456789"));     // sin letra
        assertFalse(Validaciones.esDniONie("1234567A8"));     // letra en medio
        assertFalse(Validaciones.esDniONie("A1234567B"));     // NIE con letra inicial no válida
        assertFalse(Validaciones.esDniONie(""));
        assertFalse(Validaciones.esDniONie(null));
    }

    @Test
    @DisplayName("Un email con formato correcto es válido")
    void emailValido() {
        assertTrue(Validaciones.esEmail("ana.perez@example.com"));
        assertTrue(Validaciones.esEmail("pedro@clinica.es"));
    }

    @Test
    @DisplayName("Un email sin @, sin dominio o con espacios se rechaza")
    void emailIncorrecto() {
        assertFalse(Validaciones.esEmail("ana.perez"));
        assertFalse(Validaciones.esEmail("ana@"));
        assertFalse(Validaciones.esEmail("ana@clinica"));
        assertFalse(Validaciones.esEmail("ana perez@clinica.es"));
        assertFalse(Validaciones.esEmail(null));
    }

    @Test
    @DisplayName("Una fecha dd/mm/aaaa correcta se lee bien")
    void fechaValida() {
        assertEquals(LocalDate.of(1985, 3, 14), Validaciones.leerFecha("14/03/1985"));
        assertEquals(LocalDate.of(2008, 2, 29), Validaciones.leerFecha("29/02/2008"));   // año bisiesto
    }

    @Test
    @DisplayName("La fecha vacía se permite porque es opcional")
    void fechaVacia() {
        assertNull(Validaciones.leerFecha(""));
        assertNull(Validaciones.leerFecha("   "));
    }

    @Test
    @DisplayName("Una fecha con otro formato o que no existe se rechaza")
    void fechaIncorrecta() {
        assertThrows(DateTimeParseException.class, () -> Validaciones.leerFecha("1985-03-14"));
        assertThrows(DateTimeParseException.class, () -> Validaciones.leerFecha("31/02/2000"));  // no existe
        assertThrows(DateTimeParseException.class, () -> Validaciones.leerFecha("29/02/2025"));  // no es bisiesto
    }
}
