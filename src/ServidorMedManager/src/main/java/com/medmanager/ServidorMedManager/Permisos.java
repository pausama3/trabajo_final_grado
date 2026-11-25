package com.medmanager.ServidorMedManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.medmanager.ServidorMedManager.modelo.Trabajador.Rol;
import com.medmanager.comun.Acciones;

/**
 * Control de acceso basado en roles.
 * <p>
 * Define qué roles pueden ejecutar cada acción del protocolo. El servidor lo
 * consulta antes de procesar cualquier petición, de modo que la seguridad no
 * depende de lo que muestre o oculte la interfaz del cliente.
 */
public final class Permisos {
    private Permisos() { }   // clase de utilidades: no se instancia

    // acción -> roles que pueden ejecutarla (iremos añadiendo una fila por acción nueva)
    private static final Map<String, Set<Rol>> PERMITIDOS = new HashMap<>();
    static {
        // Común a todos los roles (el Conversor oculta las alergias a recepción, RN3)
        PERMITIDOS.put(Acciones.BUSCAR_PACIENTES,
                EnumSet.of(Rol.RECEPCIONISTA, Rol.MEDICO, Rol.ENFERMERO));

        // Médico
        PERMITIDOS.put(Acciones.AGENDA_MEDICO,
                EnumSet.of(Rol.MEDICO));
        PERMITIDOS.put(Acciones.BUSCAR_INFORMES,
                EnumSet.of(Rol.MEDICO));
        PERMITIDOS.put(Acciones.GUARDAR_INFORME,
                EnumSet.of(Rol.MEDICO));

        // Recepción
        for (String accion : new String[] {Acciones.AGENDA_CLINICA, Acciones.LISTAR_MEDICOS,
                Acciones.CREAR_CITA, Acciones.CANCELAR_CITA, Acciones.REPROGRAMAR_CITA,
                Acciones.GUARDAR_PACIENTE, Acciones.BAJA_PACIENTE}) {
            PERMITIDOS.put(accion, EnumSet.of(Rol.RECEPCIONISTA));
        }

        // Enfermería (app Android)
        PERMITIDOS.put(Acciones.CITAS_PACIENTE_HOY,
                EnumSet.of(Rol.ENFERMERO));
        PERMITIDOS.put(Acciones.GUARDAR_OBSERVACIONES,
                EnumSet.of(Rol.ENFERMERO));
    }

    /**
     * Indica si un rol puede ejecutar una acción.
     * Denegar por defecto: si la acción no está en el mapa, nadie puede ejecutarla.
     *
     * @param rol    rol del trabajador en sesión
     * @param accion acción solicitada (ver {@link Acciones})
     * @return {@code true} si la acción está permitida para ese rol
     */
    public static boolean puede(Rol rol, String accion) {
        Set<Rol> roles = PERMITIDOS.get(accion);
        return roles != null && roles.contains(rol);
    }
}