package com.medmanager.ServidorMedManager;

import java.sql.SQLException;

/**
 * Traduce los errores de MySQL a mensajes que entiende el usuario.
 * Hibernate envuelve la SQLException original en varias capas, así que se busca en la cadena de causas.
 * <p>
 * De este modo las reglas que protege la base de datos (restricciones UNIQUE,
 * CHECK y triggers) llegan al cliente como avisos comprensibles.
 */
public final class ErroresBD {
    private ErroresBD() { }   // clase de utilidades: no se instancia

    // ---- Códigos de error de MySQL que se tratan de forma específica ----
    private static final int DUPLICADO        = 1062;   // UNIQUE
    private static final int TRIGGER_SIGNAL   = 1644;   // SIGNAL SQLSTATE '45000' en un trigger
    private static final int CHECK_FALLIDO    = 3819;   // CHECK constraint
    private static final int SIN_PERMISO      = 1142;   // el usuario de la app no tiene ese privilegio

    /**
     * Construye un mensaje legible a partir de cualquier excepción de acceso a datos.
     *
     * @param error excepción capturada (normalmente lanzada por Hibernate)
     * @return mensaje listo para mostrar al usuario
     */
    public static String mensajeParaUsuario(Throwable error) {
        SQLException sql = buscarSQLException(error);
        if (sql == null) {
            return "No se pudo completar la operación en el servidor";
        }
        String texto = sql.getMessage() != null ? sql.getMessage() : "";

        switch (sql.getErrorCode()) {
            case DUPLICADO:
                // El nombre de la restricción violada aparece en el mensaje de MySQL
                if (texto.contains("uq_cita_medico_franja"))   return "El médico ya tiene una cita a esa hora";
                if (texto.contains("uq_cita_paciente_franja")) return "El paciente ya tiene otra cita a esa hora";
                if (texto.contains("uq_historial_cita"))       return "Esta cita ya tiene un informe";
                if (texto.toLowerCase().contains("dni"))       return "Ya existe una persona con ese DNI";
                return "Ese registro ya existe";
            case TRIGGER_SIGNAL:
                return texto;   // el mensaje del trigger ya está escrito para el usuario
            case CHECK_FALLIDO:
                if (texto.contains("chk_cita_franja")) return "Las citas van en franjas de 15 minutos";
                if (texto.contains("chk_cita_estado")) return "Estado de cita no válido";
                return "Los datos no cumplen las reglas de la base de datos";
            case SIN_PERMISO:
                return "Operación no permitida en la base de datos";
            default:
                return "Error de base de datos (" + sql.getErrorCode() + ")";
        }
    }

    /**
     * Recorre la cadena de causas hasta encontrar la {@link SQLException} original.
     *
     * @return la excepción SQL, o {@code null} si el error no procede de la base de datos
     */
    private static SQLException buscarSQLException(Throwable t) {
        while (t != null) {
            if (t instanceof SQLException) return (SQLException) t;
            t = t.getCause();
        }
        return null;
    }
}
