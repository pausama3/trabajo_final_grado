package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Objeto de transferencia (DTO) que representa una entrada del historial
 * médico de un paciente, es decir, el informe redactado al cerrar una cita.
 */
public class HistorialDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idPaciente;
    private String nombrePaciente;
    private String nombreMedico;   // quién redactó el informe (el médico de la cita)
    private int idCita;            // cita de la que procede el informe
    private String diagnostico;
    private String tratamiento;
    private LocalDateTime fechaRegistro;   // momento en el que se guardó el informe

    /**
     * Construye una entrada de historial con todos sus datos.
     *
     * @param id             identificador del registro de historial
     * @param idPaciente     identificador del paciente
     * @param nombrePaciente nombre completo del paciente
     * @param nombreMedico   nombre completo del médico que firmó el informe
     * @param idCita         identificador de la cita asociada
     * @param diagnostico    diagnóstico emitido
     * @param tratamiento    tratamiento prescrito
     * @param fechaRegistro  fecha y hora de registro del informe
     */
    public HistorialDTO(int id, int idPaciente, String nombrePaciente, String nombreMedico, int idCita, String diagnostico,
                        String tratamiento, LocalDateTime fechaRegistro) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.nombreMedico = nombreMedico;
        this.idCita = idCita;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fechaRegistro = fechaRegistro;
    }

    // Getters
    public int getId() { return id; }
    public int getIdPaciente() { return idPaciente; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getNombreMedico() { return nombreMedico; }
    public int getIdCita() { return idCita; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}
