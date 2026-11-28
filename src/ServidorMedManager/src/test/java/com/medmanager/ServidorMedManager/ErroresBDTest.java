package com.medmanager.ServidorMedManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de la clase ErroresBD.
 * Simulan los errores que devuelve MySQL (envueltos en otra excepción, como hace Hibernate)
 * y comprueban que se traducen al mensaje que verá el usuario.
 */
class ErroresBDTest {

    /** Crea un error de MySQL con su código, envuelto como lo haría Hibernate. */
    private static Throwable errorMySQL(int codigo, String mensaje) {
        SQLException original = new SQLException(mensaje, "23000", codigo);
        return new RuntimeException("Error de Hibernate", original);
    }

    @Test
    @DisplayName("RN1: médico ocupado a esa hora")
    void medicoOcupado() {
        Throwable e = errorMySQL(1062,
                "Duplicate entry '2-2027-01-15 10:00:00' for key 'cita.uq_cita_medico_franja'");
        assertEquals("El médico ya tiene una cita a esa hora", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("RN2: paciente con otra cita a esa hora")
    void pacienteOcupado() {
        Throwable e = errorMySQL(1062,
                "Duplicate entry '1-2027-01-15 10:00:00' for key 'cita.uq_cita_paciente_franja'");
        assertEquals("El paciente ya tiene otra cita a esa hora", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("Una cita solo puede tener un informe")
    void citaConInforme() {
        Throwable e = errorMySQL(1062, "Duplicate entry '3' for key 'historial_medico.uq_historial_cita'");
        assertEquals("Esta cita ya tiene un informe", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("DNI duplicado")
    void dniDuplicado() {
        Throwable e = errorMySQL(1062, "Duplicate entry '55555555E' for key 'paciente.dni'");
        assertEquals("Ya existe una persona con ese DNI", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("El mensaje de un trigger llega tal cual al usuario")
    void mensajeDeTrigger() {
        Throwable e = errorMySQL(1644, "La cita solo puede asignarse a un trabajador con rol MEDICO");
        assertEquals("La cita solo puede asignarse a un trabajador con rol MEDICO",
                ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("Cita fuera de una franja de 15 minutos")
    void franjaIncorrecta() {
        Throwable e = errorMySQL(3819, "Check constraint 'chk_cita_franja' is violated.");
        assertEquals("Las citas van en franjas de 15 minutos", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("Operación sin permiso en la base de datos")
    void sinPermiso() {
        Throwable e = errorMySQL(1142, "DELETE command denied to user 'medmanager_app'@'localhost'");
        assertEquals("Operación no permitida en la base de datos", ErroresBD.mensajeParaUsuario(e));
    }

    @Test
    @DisplayName("Un error que no viene de la base de datos da un mensaje genérico")
    void errorNoSQL() {
        Throwable e = new IllegalStateException("Fallo interno");
        assertEquals("No se pudo completar la operación en el servidor", ErroresBD.mensajeParaUsuario(e));
    }
}
