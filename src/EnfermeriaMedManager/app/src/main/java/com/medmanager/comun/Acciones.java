package com.medmanager.comun;

/**
 * Catálogo de acciones del protocolo de comunicación cliente-servidor.
 * <p>
 * Cada petición viaja dentro de un {@link PaqueteRed} cuyo campo {@code accion}
 * toma uno de estos valores. Junto a cada constante se documenta el tipo de
 * <em>payload</em> que envía el cliente y el tipo de dato que devuelve el
 * servidor en la respuesta {@link #OK}.
 * <p>
 * Esta clase es idéntica en el proyecto de escritorio y en el del servidor:
 * ambos extremos deben compartir exactamente los mismos literales.
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

    // ---- Común a médico y recepción ----

    /** Búsqueda de pacientes por nombre, apellidos o DNI. */
    public static final String BUSCAR_PACIENTES = "BUSCAR_PACIENTES";   // payload: String  -> List<PacienteDTO>

    // ---- Médico ----
    /** Citas del médico autenticado para un día concreto. */
    public static final String AGENDA_MEDICO   = "AGENDA_MEDICO";       // payload: LocalDate  -> List<CitaDTO>
    /** Búsqueda de informes clínicos (historial) de los pacientes. */
    public static final String BUSCAR_INFORMES = "BUSCAR_INFORMES";     // payload: String     -> List<HistorialDTO>

    // ---- Recepción ----
    /** Citas de todos los médicos de la clínica para un día concreto. */
    public static final String AGENDA_CLINICA   = "AGENDA_CLINICA";     // payload: LocalDate   -> List<CitaDTO>
    /** Listado de médicos con su especialidad (para asignar citas). */
    public static final String LISTAR_MEDICOS   = "LISTAR_MEDICOS";     // payload: null        -> List<TrabajadorDTO>
    /** Añadir observaciones. */
    public static final String GUARDAR_OBSERVACIONES   = "GUARDAR_OBSERVACIONES";
}
