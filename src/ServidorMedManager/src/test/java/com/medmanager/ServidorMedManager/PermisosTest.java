package com.medmanager.ServidorMedManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.medmanager.ServidorMedManager.modelo.Trabajador.Rol;
import com.medmanager.comun.Acciones;

/**
 * Pruebas unitarias de la clase Permisos (RF1, RN3 y RN4).
 * Comprueban que cada rol solo puede ejecutar sus acciones y que
 * cualquier acción que no esté en la tabla se deniega por defecto.
 */
class PermisosTest {

    @Test
    @DisplayName("Los tres roles pueden buscar pacientes")
    void todosPuedenBuscarPacientes() {
        for (Rol rol : Rol.values()) {
            assertTrue(Permisos.puede(rol, Acciones.BUSCAR_PACIENTES), rol + " debería poder buscar pacientes");
        }
    }

    @Test
    @DisplayName("Recepción puede gestionar la agenda, las citas y los pacientes")
    void recepcionPuedeSusAcciones() {
        String[] acciones = {Acciones.AGENDA_CLINICA, Acciones.LISTAR_MEDICOS, Acciones.CREAR_CITA,
                Acciones.CANCELAR_CITA, Acciones.REPROGRAMAR_CITA, Acciones.GUARDAR_PACIENTE,
                Acciones.BAJA_PACIENTE};
        for (String accion : acciones) {
            assertTrue(Permisos.puede(Rol.RECEPCIONISTA, accion), "Recepción debería poder " + accion);
        }
    }

    @Test
    @DisplayName("El médico puede ver su agenda y consultar y guardar informes")
    void medicoPuedeSusAcciones() {
        assertTrue(Permisos.puede(Rol.MEDICO, Acciones.AGENDA_MEDICO));
        assertTrue(Permisos.puede(Rol.MEDICO, Acciones.BUSCAR_INFORMES));
        assertTrue(Permisos.puede(Rol.MEDICO, Acciones.GUARDAR_INFORME));
    }

    @Test
    @DisplayName("Enfermería puede ver las citas de hoy y guardar observaciones")
    void enfermeriaPuedeSusAcciones() {
        assertTrue(Permisos.puede(Rol.ENFERMERO, Acciones.CITAS_PACIENTE_HOY));
        assertTrue(Permisos.puede(Rol.ENFERMERO, Acciones.GUARDAR_OBSERVACIONES));
    }

    @Test
    @DisplayName("RN4: solo el médico puede escribir informes")
    void soloElMedicoGuardaInformes() {
        assertFalse(Permisos.puede(Rol.ENFERMERO, Acciones.GUARDAR_INFORME));
        assertFalse(Permisos.puede(Rol.RECEPCIONISTA, Acciones.GUARDAR_INFORME));
    }

    @Test
    @DisplayName("RN3: recepción no puede consultar el historial clínico")
    void recepcionNoVeInformes() {
        assertFalse(Permisos.puede(Rol.RECEPCIONISTA, Acciones.BUSCAR_INFORMES));
    }

    @Test
    @DisplayName("Enfermería no puede crear, cancelar ni reprogramar citas")
    void enfermeriaNoGestionaCitas() {
        assertFalse(Permisos.puede(Rol.ENFERMERO, Acciones.CREAR_CITA));
        assertFalse(Permisos.puede(Rol.ENFERMERO, Acciones.CANCELAR_CITA));
        assertFalse(Permisos.puede(Rol.ENFERMERO, Acciones.REPROGRAMAR_CITA));
    }

    @Test
    @DisplayName("El médico no puede gestionar citas ni dar de baja pacientes")
    void medicoNoHaceTareasDeRecepcion() {
        assertFalse(Permisos.puede(Rol.MEDICO, Acciones.CREAR_CITA));
        assertFalse(Permisos.puede(Rol.MEDICO, Acciones.BAJA_PACIENTE));
        assertFalse(Permisos.puede(Rol.MEDICO, Acciones.GUARDAR_OBSERVACIONES));
    }

    @Test
    @DisplayName("Una acción que no existe se deniega a todos los roles")
    void accionDesconocidaSeDeniega() {
        for (Rol rol : Rol.values()) {
            assertFalse(Permisos.puede(rol, "BORRAR_TODO"));
            assertFalse(Permisos.puede(rol, null));
        }
    }
}
