package com.medmanager.ServidorMedManager;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Historial_Medico;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.HistorialDTO;
import com.medmanager.comun.PacienteDTO;

/** Pasa de entidades de Hibernate a DTO. Aquí se decide qué campos salen del servidor. */
public final class Conversor {
    private Conversor() { }

    public static PacienteDTO aDTO(Paciente p, boolean conAlergias) {
        return new PacienteDTO(
                p.getId_paciente(), p.getDni(), p.getNombre(), p.getApellidos(),
                p.getTelefono(), p.getEmail(), p.getDireccion(), p.getFecha_nacimiento(),
                conAlergias ? p.getAlergias() : null,   // RN3: sin permiso, no sale
                p.isActivo());
    }

    public static CitaDTO aDTO(Cita c, boolean conAlergias) {
        Paciente p = c.getPaciente();
        return new CitaDTO(
                c.getId_cita(), p.getId_paciente(), nombreCompleto(p),
                conAlergias ? p.getAlergias() : null,   // RN3
                c.getTrabajador().getId_trabajador(), c.getFecha_hora(), c.getEstado(),
                c.getMotivo_consulta(), c.getObservaciones());
    }

    public static HistorialDTO aDTO(Historial_Medico h) {
        Paciente p = h.getPaciente();
        return new HistorialDTO(
                h.getId_historial(), p.getId_paciente(), nombreCompleto(p),
                h.getCita().getId_cita(), h.getDiagnostico(), h.getTratamiento(), h.getFecha_registro());
    }

    private static String nombreCompleto(Paciente p) {
        return p.getNombre() + " " + p.getApellidos();
    }
}
