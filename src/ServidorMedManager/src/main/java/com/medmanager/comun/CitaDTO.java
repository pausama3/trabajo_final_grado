package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CitaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int idPaciente;
    private String nombrePaciente;
    private String alergiasPaciente;   // RN3: solo se rellena para el médico; null para el resto
    private int idTrabajador;
    private LocalDateTime fechaHora;
    private String estado;
    private String motivoConsulta;
    private String observacionesEnfermeria;

    public CitaDTO(int id, int idPaciente, String nombrePaciente, String alergiasPaciente, int idTrabajador,
                   LocalDateTime fechaHora, String estado, String motivoConsulta, String observacionesEnfermeria) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.nombrePaciente = nombrePaciente;
        this.alergiasPaciente = alergiasPaciente;
        this.idTrabajador = idTrabajador;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.motivoConsulta = motivoConsulta;
        this.observacionesEnfermeria = observacionesEnfermeria;
    }

    // Getters
    public int getId() { return id; }
    public int getIdPaciente() { return idPaciente; }
    public String getNombrePaciente() { return nombrePaciente; }
    public String getAlergiasPaciente() { return alergiasPaciente; }
    public int getIdTrabajador() { return idTrabajador; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public String getEstado() { return estado; }
    public String getMotivoConsulta() { return motivoConsulta; }
    public String getObservacionesEnfermeria() { return observacionesEnfermeria; }
}
