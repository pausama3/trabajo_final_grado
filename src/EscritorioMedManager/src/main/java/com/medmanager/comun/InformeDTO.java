package com.medmanager.comun;

import java.io.Serializable;

/** Lo que el médico envía al cerrar una consulta: el informe y las alergias actualizadas. */
public class InformeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int idCita;
    private final String diagnostico;
    private final String tratamiento;
    private final String alergias;

    public InformeDTO(int idCita, String diagnostico, String tratamiento, String alergias) {
        this.idCita = idCita;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.alergias = alergias;
    }

    public int getIdCita() { return idCita; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public String getAlergias() { return alergias; }
}
