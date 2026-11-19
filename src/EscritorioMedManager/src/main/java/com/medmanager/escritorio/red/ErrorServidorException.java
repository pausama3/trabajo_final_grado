package com.medmanager.escritorio.red;

/**
 * El servidor ha respondido con ERROR: el mensaje es el que se muestra al usuario.
 * <p>
 * Es una excepción comprobada para obligar a la interfaz a tratar los errores
 * de negocio (datos inválidos, permisos, conflictos...) de forma explícita.
 */
public class ErrorServidorException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * @param mensaje texto del error enviado por el servidor
     */
    public ErrorServidorException(String mensaje) {
        super(mensaje);
    }
}