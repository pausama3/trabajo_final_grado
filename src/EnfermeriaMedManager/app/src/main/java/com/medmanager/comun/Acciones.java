package com.medmanager.comun;

/**
 * Catálogo de acciones del protocolo de comunicación cliente-servidor.
 * <p>
 * Cada petición viaja dentro de un {@link PaqueteRed} cuyo campo {@code accion}
 * toma uno de estos valores. Junto a cada constante se documenta el tipo de
 * <em>payload</em> que envía el cliente y el tipo de dato que devuelve el
 * servidor en la respuesta {@link #OK}.
 * <p>
 * Esta clase es idéntica en los tres proyectos (servidor, escritorio y Android):
 * todos los extremos deben compartir exactamente los mismos literales.
 */
public final class Acciones {
    private Acciones() { }   // no se instancia: solo contiene constantes

    // ---- Control de sesión y respuestas genéricas ----

    /** Inicio de sesión. Payload: {@link LoginDTO} → {@link TrabajadorDTO} del usuario autenticado. */
    public static final String LOGIN = "LOGIN";
    /** Respuesta del servidor cuando la petición se ha procesado correctamente. */
    public static final String OK    = "OK";
    /** Respuesta del servidor cuando la petición falla; el payload es el mensaje para el usuario. */
    public static final String ERROR = "ERROR";

    // ---- Común a todos los roles (recepción, médico y enfermería) ----

    /**
     * Búsqueda de pacientes por nombre, apellidos o DNI. Las alergias solo se
     * incluyen para el personal sanitario (médico y enfermería); recepción no las ve (RN3).
     */
    public static final String BUSCAR_PACIENTES = "BUSCAR_PACIENTES";   // payload: String  -> List<PacienteDTO>

    // ---- Médico ----
    /** Citas del médico autenticado para un día concreto. */
    public static final String AGENDA_MEDICO   = "AGENDA_MEDICO";       // payload: LocalDate  -> List<CitaDTO>
    /** Búsqueda de informes clínicos (historial) de los pacientes. */
    public static final String BUSCAR_INFORMES = "BUSCAR_INFORMES";     // payload: String     -> List<HistorialDTO>
    /** Registro del informe que cierra una consulta. */
    public static final String GUARDAR_INFORME = "GUARDAR_INFORME";     // payload: InformeDTO -> HistorialDTO

    // ---- Recepción ----
    /** Citas de todos los médicos de la clínica para un día concreto. */
    public static final String AGENDA_CLINICA   = "AGENDA_CLINICA";     // payload: LocalDate   -> List<CitaDTO>
    /** Listado de médicos con su especialidad (para asignar citas). */
    public static final String LISTAR_MEDICOS   = "LISTAR_MEDICOS";     // payload: null        -> List<TrabajadorDTO>
    /** Alta de una cita nueva. */
    public static final String CREAR_CITA       = "CREAR_CITA";         // payload: CitaDTO     -> CitaDTO
    /** Cancelación de una cita pendiente. */
    public static final String CANCELAR_CITA    = "CANCELAR_CITA";      // payload: Integer id  -> CitaDTO
    /** Cambio de fecha y hora de una cita pendiente. */
    public static final String REPROGRAMAR_CITA = "REPROGRAMAR_CITA";   // payload: CitaDTO (id + nueva fecha/hora) -> CitaDTO
    /** Alta (id 0) o modificación de los datos de un paciente. */
    public static final String GUARDAR_PACIENTE = "GUARDAR_PACIENTE";   // payload: PacienteDTO (id 0 = alta) -> PacienteDTO
    /** Baja lógica de un paciente. */
    public static final String BAJA_PACIENTE    = "BAJA_PACIENTE";      // payload: Integer id  -> String (resumen)

    // ---- Enfermería (app Android) ----
    /** Citas de hoy de un paciente, para la ficha rápida (los datos y alergias llegan con BUSCAR_PACIENTES). */
    public static final String CITAS_PACIENTE_HOY    = "CITAS_PACIENTE_HOY";     // payload: Integer idPaciente -> List<CitaDTO>
    /** Registro de las observaciones de enfermería en una cita pendiente de hoy (RN4). */
    public static final String GUARDAR_OBSERVACIONES = "GUARDAR_OBSERVACIONES";  // payload: CitaDTO (id + observaciones) -> CitaDTO
}
