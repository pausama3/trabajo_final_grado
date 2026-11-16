package com.medmanager.comun;

public final class Acciones {
    private Acciones() { }   // no se instancia: solo contiene constantes

    public static final String LOGIN = "LOGIN";
    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";

    public static final String BUSCAR_PACIENTES = "BUSCAR_PACIENTES";   // payload: String  -> List<PacienteDTO>

    // ---- Médico ----
    public static final String AGENDA_MEDICO   = "AGENDA_MEDICO";       // payload: LocalDate  -> List<CitaDTO>
    public static final String INFORMES_MEDICO = "INFORMES_MEDICO";     // payload: null       -> List<HistorialDTO>
    public static final String GUARDAR_INFORME = "GUARDAR_INFORME";     // payload: InformeDTO -> HistorialDTO
}
