package com.medmanager.comun;

import java.io.Serializable;

/**
 * Unidad de información que se intercambia por el socket entre cliente y servidor.
 * <p>
 * Se compone de una {@code accion} (una de las constantes de {@link Acciones})
 * y de un {@code payload} opcional con los datos asociados. Se usa tanto para
 * las peticiones como para las respuestas ({@link Acciones#OK} / {@link Acciones#ERROR}).
 */
public class PaqueteRed implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String accion;   // qué se pide o qué resultado se devuelve
    private final Object payload;  // datos asociados; debe ser serializable o null

    /**
     * @param accion  acción del protocolo (ver {@link Acciones})
     * @param payload datos que acompañan a la acción
     */
    public PaqueteRed(String accion, Object payload) {
        this.accion = accion;
        this.payload = payload;
    }

    /**
     * Crea un paquete sin datos asociados.
     *
     * @param accion acción del protocolo (ver {@link Acciones})
     */
    public PaqueteRed(String accion) {
        this(accion, null);
    }

    // Getters
    public String getAccion() { return accion; }
    public Object getPayload() { return payload; }
}