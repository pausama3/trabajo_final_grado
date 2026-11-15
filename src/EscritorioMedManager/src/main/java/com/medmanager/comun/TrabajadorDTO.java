package com.medmanager.comun;

import java.io.Serializable;

public class TrabajadorDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final int id;
    private final String nombre;
    private final String apellidos;
    private final String rol;

    public TrabajadorDTO(int id, String nombre, String apellidos, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getRol() { return rol; }
}