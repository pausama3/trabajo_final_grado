package com.medmanager.ServidorMedManager;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Historial_Medico;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.HistorialDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.TrabajadorDTO;

/**
 * Pasa de entidades de Hibernate a DTO. Aquí se decide qué campos salen del servidor.
 * <p>
 * Las entidades nunca se envían directamente al cliente: así se evitan problemas
 * de carga perezosa fuera de la sesión de Hibernate y se aplica en un único
 * punto la regla de confidencialidad RN3.
 */
public final class Conversor {
    private Conversor() { }   // clase de utilidades: no se instancia

    /**
     * @param p           paciente a convertir
     * @param conAlergias {@code true} si el rol del usuario puede ver las alergias
     * @return DTO del paciente
     */
    public static PacienteDTO aDTO(Paciente p, boolean conAlergias) {
        return new PacienteDTO(
                p.getId_paciente(), p.getDni(), p.getNombre(), p.getApellidos(),
                p.getTelefono(), p.getEmail(), p.getDireccion(), p.getFecha_nacimiento(),
                conAlergias ? p.getAlergias() : null,   // RN3: sin permiso, no sale
                p.isActivo());
    }

    /**
     * @param c             cita a convertir
     * @param datosClinicos true para médico/enfermería; false para recepción, que según RN3
     *                      no puede ver ni las alergias ni las observaciones de enfermería
     * @return DTO de la cita con los nombres de paciente y médico ya resueltos
     */
    public static CitaDTO aDTO(Cita c, boolean datosClinicos) {
        Paciente p = c.getPaciente();
        Trabajador m = c.getTrabajador();
        return new CitaDTO(
                c.getId_cita(), p.getId_paciente(), nombreCompleto(p),
                datosClinicos ? p.getAlergias() : null,   // RN3
                m.getId_trabajador(), m.getNombre() + " " + m.getApellidos(),
                c.getFecha_hora(), c.getEstado(), c.getMotivo_consulta(),
                datosClinicos ? c.getObservaciones() : null);   // RN3
    }

    /**
     * @param h entrada de historial a convertir
     * @return DTO del informe; el médico es el que atendió la cita asociada
     */
    public static HistorialDTO aDTO(Historial_Medico h) {
        Paciente p = h.getPaciente();
        Trabajador medico = h.getCita().getTrabajador();
        return new HistorialDTO(
                h.getId_historial(), p.getId_paciente(), nombreCompleto(p),
                medico.getNombre() + " " + medico.getApellidos(),
                h.getCita().getId_cita(), h.getDiagnostico(), h.getTratamiento(), h.getFecha_registro());
    }

    /** Datos públicos de un trabajador (nunca el usuario ni el hash de la contraseña). */
    public static TrabajadorDTO aDTO(Trabajador t) {
        return new TrabajadorDTO(
                t.getId_trabajador(), t.getNombre(), t.getApellidos(), t.getRol().name(),
                t.getEspecialidad() != null ? t.getEspecialidad().getNombre_especialidad() : null);
    }

    /** Nombre y apellidos del paciente en una sola cadena. */
    private static String nombreCompleto(Paciente p) {
        return p.getNombre() + " " + p.getApellidos();
    }
}
