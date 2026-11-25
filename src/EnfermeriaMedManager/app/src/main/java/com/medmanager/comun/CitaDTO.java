package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Objeto de transferencia (DTO) que representa una cita médica.
 * <p>
 * Viaja serializado entre cliente y servidor. Además de los identificadores,
 * incluye datos ya resueltos (nombre del paciente y del médico) para que la
 * interfaz pueda mostrarlos sin hacer peticiones adicionales.
 */
public class CitaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;                          // 0 si la cita aún no existe en la base de datos
    private int idPaciente;
    private String nombrePaciente;           // nombre completo, listo para mostrar
    private String alergiasPaciente;   // RN3: solo se rellena para médico y enfermería; null para recepción
    private int idTrabajador;                // médico que atiende la cita
    private String nombreMedico;             // nombre completo del médico, listo para mostrar
    private LocalDateTime fechaHora;
    private String estado;                   // estado de la cita (pendiente, realizada, cancelada...)
    private String motivoConsulta;
    private String observacionesEnfermeria;

    /**
     * Construye una cita con todos sus datos.
     *
     * @param id                      identificador de la cita (0 si es nueva)
     * @param idPaciente              identificador del paciente
     * @param nombrePaciente          nombre completo del paciente
     * @param alergiasPaciente        alergias del paciente (solo visibles para el médico)
     * @param idTrabajador            identificador del médico asignado
     * @param nombreMedico            nombre completo del médico
     * @param fechaHora               fecha y hora de la cita
     * @param estado                  estado actual de la cita
     * @param motivoConsulta          motivo indicado al pedir la cita
     * @param observacionesEnfermeria anotaciones de enfermería, si las hay
     */
    public CitaDTO(int id, int idPaciente, String nombrePaciente, String alergiasPaciente,
                   int idTrabajador, String nombreMedico, LocalDateTime fechaHora, String estado,
                   String motivoConsulta, String observacionesEnfermeria) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.alergiasPaciente = alergiasPaciente;
        this.idTrabajador = idTrabajador;
        this.nombreMedico = nombreMedico;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivoConsulta = motivoConsulta;
        this.observacionesEnfermeria = observacionesEnfermeria;
    }

    /**
     * Para pedir una cita nueva: solo hacen falta paciente, médico, fecha/hora y motivo.
     *
     * @param idPaciente paciente que solicita la cita
     * @param idMedico   médico que la atenderá
     * @param fechaHora  fecha y hora solicitadas
     * @param motivo     motivo de la consulta
     * @return DTO listo para enviarse con la acción {@link Acciones#CREAR_CITA}
     */
    public static CitaDTO nueva(int idPaciente, int idMedico, LocalDateTime fechaHora, String motivo) {
        return new CitaDTO(0, idPaciente, null, null, idMedico, null, fechaHora, null, motivo, null);
    }

    /**
     * Para reprogramar una cita: solo hacen falta su id y la nueva fecha/hora (el médico no cambia).
     *
     * @param idCita         cita que se quiere mover
     * @param nuevaFechaHora nueva fecha y hora
     * @return DTO listo para enviarse con la acción {@link Acciones#REPROGRAMAR_CITA}
     */
    public static CitaDTO reprogramada(int idCita, LocalDateTime nuevaFechaHora) {
        return new CitaDTO(idCita, 0, null, null, 0, null, nuevaFechaHora, null, null, null);
    }

    /**
     * Para guardar las observaciones de enfermería: solo hacen falta el id de la cita y el texto.
     *
     * @param idCita        cita en la que se anotan las observaciones
     * @param observaciones texto de enfermería (vacío o null las borra)
     * @return DTO listo para enviarse con la acción {@link Acciones#GUARDAR_OBSERVACIONES}
     */
    public static CitaDTO conObservaciones(int idCita, String observaciones) {
        return new CitaDTO(idCita, 0, null, null, 0, null, null, null, null, observaciones);
    }

    // Getters
    public int getId() { return id; }
    public int getIdPaciente() { return idPaciente; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getAlergiasPaciente() { return alergiasPaciente; }
    public int getIdTrabajador() { return idTrabajador; }
    public String getNombreMedico() { return nombreMedico; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public String getObservacionesEnfermeria() { return observacionesEnfermeria; }
}
