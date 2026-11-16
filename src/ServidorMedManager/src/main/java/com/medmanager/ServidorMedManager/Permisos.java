package com.medmanager.ServidorMedManager;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.medmanager.ServidorMedManager.modelo.Trabajador.Rol;
import com.medmanager.comun.Acciones;

public final class Permisos {
    private Permisos() { }

    // acción -> roles que pueden ejecutarla (iremos añadiendo una fila por acción nueva)
    private static final Map<String, Set<Rol>> PERMITIDOS = new HashMap<>();
    static {
        PERMITIDOS.put(Acciones.BUSCAR_PACIENTES,
                EnumSet.of(Rol.RECEPCIONISTA, Rol.MEDICO, Rol.ENFERMERO));
        PERMITIDOS.put(Acciones.AGENDA_MEDICO,
                EnumSet.of(Rol.MEDICO));
        PERMITIDOS.put(Acciones.INFORMES_MEDICO,
                EnumSet.of(Rol.MEDICO));
        PERMITIDOS.put(Acciones.GUARDAR_INFORME,
                EnumSet.of(Rol.MEDICO));
    }

    // Denegar por defecto: si la acción no está en el mapa, nadie puede ejecutarla.
    public static boolean puede(Rol rol, String accion) {
        Set<Rol> roles = PERMITIDOS.get(accion);
        return roles != null && roles.contains(rol);
    }
}