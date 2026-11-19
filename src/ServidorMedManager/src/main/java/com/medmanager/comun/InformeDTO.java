package com.medmanager.comun;

import java.io.Serializable;

/**
 * Lo que el médico envía al cerrar una consulta: el informe y las alergias actualizadas.
 * <p>
 * Es inmutable: todos sus campos se fijan en el constructor.
 */
public class InformeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int idCita;          // cita que se cierra con este informe
    private final String diagnostico;
    private final String tratamiento;
    private final String alergias;     // alergias del paciente, revisadas por el médico

    /**
     * @param idCita      identificador de la cita atendida
     * @param diagnostico diagnóstico emitido
     * @param tratamiento tratamiento prescrito
     * @param alergias    alergias del paciente tras la revisión
     */
    public InformeDTO(int idCita, String diagnostico, String tratamiento, String alergias) {
        this.idCita = idCita;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.alergias = alergias;
    }

    // Getters
    public int getIdCita() { return idCita; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public String getAlergias() { return alergias; }
}
