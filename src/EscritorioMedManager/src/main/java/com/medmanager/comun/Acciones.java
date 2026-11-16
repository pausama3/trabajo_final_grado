package com.medmanager.comun;

public final class Acciones {
    private Acciones() { }   // no se instancia: solo contiene constantes

    public static final String LOGIN = "LOGIN";
    public static final String OK    = "OK";
    public static final String ERROR = "ERROR";

    public static final String BUSCAR_PACIENTES = "BUSCAR_PACIENTES";   // payload: String  -> List<PacienteDTO>

    // ---- Médico ----
    public static final String AGENDA_MEDICO   = "AGENDA_MEDICO";       // payload: LocalDate  -> List<CitaDTO>
    public static final String BUSCAR_INFORMES = "BUSCAR_INFORMES";     // payload: String     -> List<HistorialDTO>
    public static final String GUARDAR_INFORME = "GUARDAR_INFORME";     // payload: InformeDTO -> HistorialDTO

    // ---- Recepción ----
    public static final String AGENDA_CLINICA   = "AGENDA_CLINICA";     // payload: LocalDate   -> List<CitaDTO>
    public static final String LISTAR_MEDICOS   = "LISTAR_MEDICOS";     // payload: null        -> List<TrabajadorDTO>
    public static final String CREAR_CITA       = "CREAR_CITA";         // payload: CitaDTO     -> CitaDTO
    public static final String CANCELAR_CITA    = "CANCELAR_CITA";      // payload: Integer id  -> CitaDTO
    public static final String GUARDAR_PACIENTE = "GUARDAR_PACIENTE";   // payload: PacienteDTO (id 0 = alta) -> PacienteDTO
    public static final String BAJA_PACIENTE    = "BAJA_PACIENTE";      // payload: Integer id  -> String (resumen)
}
