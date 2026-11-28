package com.medmanager.enfermeriamedmanager

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Pruebas unitarias de Utilidades.kt (app de enfermería).
 * Se ejecutan en el ordenador, sin emulador ni móvil.
 */
class UtilidadesTest {

    /** Fecha fija para que las pruebas de edad den siempre el mismo resultado. */
    private val hoy = LocalDate.of(2026, 11, 28)

    @Test
    fun `la edad cuenta el año el mismo día del cumpleaños`() {
        assertEquals(41, edad(LocalDate.of(1985, 11, 28), hoy))
    }

    @Test
    fun `la edad no cuenta el año el día antes del cumpleaños`() {
        assertEquals(40, edad(LocalDate.of(1985, 11, 29), hoy))
    }

    @Test
    fun `un bebé de pocos meses tiene 0 años`() {
        assertEquals(0, edad(LocalDate.of(2026, 3, 1), hoy))
    }

    @Test
    fun `un nacido el 29 de febrero cumple años el 1 de marzo en años no bisiestos`() {
        val nacimiento = LocalDate.of(2008, 2, 29)
        assertEquals(17, edad(nacimiento, LocalDate.of(2026, 2, 28)))
        assertEquals(18, edad(nacimiento, LocalDate.of(2026, 3, 1)))
    }

    @Test
    fun `la búsqueda necesita al menos 2 caracteres`() {
        assertFalse(busquedaValida(""))
        assertFalse(busquedaValida("A"))
        assertTrue(busquedaValida("An"))
        assertTrue(busquedaValida("55555555E"))
    }

    @Test
    fun `los espacios de los extremos no cuentan en la búsqueda`() {
        assertFalse(busquedaValida("   A   "))
        assertTrue(busquedaValida("  Ana  "))
    }

    @Test
    fun `las observaciones admiten hasta 2000 caracteres`() {
        assertTrue(observacionesValidas("x".repeat(2000)))
        assertFalse(observacionesValidas("x".repeat(2001)))
    }

    @Test
    fun `las observaciones vacías se permiten para poder borrarlas`() {
        assertTrue(observacionesValidas(""))
    }

    @Test
    fun `los espacios de los extremos no cuentan en las observaciones`() {
        assertTrue(observacionesValidas("x".repeat(2000) + "   "))
    }
}
