package com.medmanager.escritorio.red;

/** El servidor ha respondido con ERROR: el mensaje es el que se muestra al usuario. */
public class ErrorServidorException extends Exception {
    private static final long serialVersionUID = 1L;

    public ErrorServidorException(String mensaje) {
        super(mensaje);
    }
}