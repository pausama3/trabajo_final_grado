package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDateTime;

public class HistorialDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private int idPaciente;
    private int idCita;
    private String diagnostico;
    private String tratamiento;
    private LocalDateTime fechaRegistro;

    public HistorialDTO(int id, int idPaciente, int idCita, String diagnostico, 
                        String tratamiento, LocalDateTime fechaRegistro) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.idCita = idCita;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.fechaRegistro = fechaRegistro;
    }

    public int getId() { return id; }
    public int getIdPaciente() { return idPaciente; }
    public int getIdCita() { return idCita; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
}