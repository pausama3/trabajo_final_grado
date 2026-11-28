package com.medmanager.ServidorMedManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Especialidad;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.ServidorMedManager.modelo.Trabajador.Rol;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.TrabajadorDTO;

/**
 * Pruebas unitarias de la clase Conversor (RN3: minimización de datos).
 * Comprueban que los datos clínicos solo salen del servidor cuando el rol puede verlos.
 */
class ConversorTest {

    private Paciente ana;
    private Trabajador medico;
    private Cita cita;

    @BeforeEach
    void prepararDatos() {
        ana = new Paciente("55555555E", LocalDate.of(1985, 3, 14), "Ana", "Pérez López",
                "611555555", "ana.perez@example.com", "Calle Mayor 1", "Penicilina", true);
        medico = new Trabajador("22222222B", "Carlos", "Martín Pons", "600222222", Rol.MEDICO,
                "medico1", "$2a$10$hash", new Especialidad("Medicina General", "Consulta general"));
        cita = new Cita(ana, medico, LocalDateTime.of(2027, 1, 15, 10, 0), "PENDIENTE",
                "Revisión anual", "Tensión 12/8");
    }

    @Test
    @DisplayName("RN3: el paciente que recibe recepción no lleva las alergias")
    void pacienteSinAlergiasParaRecepcion() {
        PacienteDTO dto = Conversor.aDTO(ana, false);
        assertNull(dto.getAlergias());
        // el resto de datos sí llegan
        assertEquals("55555555E", dto.getDni());
        assertEquals("Ana", dto.getNombre());
        assertEquals("611555555", dto.getTelefono());
    }

    @Test
    @DisplayName("El paciente que recibe el personal sanitario lleva las alergias")
    void pacienteConAlergiasParaSanitarios() {
        PacienteDTO dto = Conversor.aDTO(ana, true);
        assertEquals("Penicilina", dto.getAlergias());
    }

    @Test
    @DisplayName("RN3: la cita que recibe recepción no lleva alergias ni observaciones")
    void citaSinDatosClinicosParaRecepcion() {
        CitaDTO dto = Conversor.aDTO(cita, false);
        assertNull(dto.getAlergiasPaciente());
        assertNull(dto.getObservacionesEnfermeria());
        // los datos organizativos sí llegan
        assertEquals("Ana Pérez López", dto.getNombrePaciente());
        assertEquals("Carlos Martín Pons", dto.getNombreMedico());
        assertEquals("Revisión anual", dto.getMotivoConsulta());
        assertEquals("PENDIENTE", dto.getEstado());
    }

    @Test
    @DisplayName("La cita que recibe el médico o enfermería lleva los datos clínicos")
    void citaConDatosClinicosParaSanitarios() {
        CitaDTO dto = Conversor.aDTO(cita, true);
        assertEquals("Penicilina", dto.getAlergiasPaciente());
        assertEquals("Tensión 12/8", dto.getObservacionesEnfermeria());
    }

    @Test
    @DisplayName("El DTO del médico incluye su especialidad y su rol")
    void trabajadorMedicoConEspecialidad() {
        TrabajadorDTO dto = Conversor.aDTO(medico);
        assertEquals("MEDICO", dto.getRol());
        assertEquals("Medicina General", dto.getEspecialidad());
    }

    @Test
    @DisplayName("El DTO de un trabajador sin especialidad la deja vacía")
    void trabajadorSinEspecialidad() {
        Trabajador recepcion = new Trabajador("11111111A", "Laura", "Gómez Ruiz", "600111111",
                Rol.RECEPCIONISTA, "recepcion1", "$2a$10$hash", null);
        TrabajadorDTO dto = Conversor.aDTO(recepcion);
        assertEquals("RECEPCIONISTA", dto.getRol());
        assertNull(dto.getEspecialidad());
    }
}
