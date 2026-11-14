package com.medmanager.comun;

import java.io.Serializable;

public class PaqueteRed implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String accion;
    private final Object payload;

    public PaqueteRed(String accion, Object payload) {
        this.accion = accion;
        this.payload = payload;
    }

    public PaqueteRed(String accion) {
        this(accion, null);
    }

    public String getAccion() { return accion; }
    public Object getPayload() { return payload; }
}