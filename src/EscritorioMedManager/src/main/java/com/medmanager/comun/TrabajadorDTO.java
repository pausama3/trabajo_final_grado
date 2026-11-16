package com.medmanager.comun;

import java.io.Serializable;

public class TrabajadorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private final String apellidos;
    private final String rol;
    private final String especialidad;   // solo médicos; null para el resto

    public TrabajadorDTO(int id, String nombre, String apellidos, String rol) {
        this(id, nombre, apellidos, rol, null);
    }

    public TrabajadorDTO(int id, String nombre, String apellidos, String rol, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.especialidad = especialidad;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getRol() { return rol; }
    public String getEspecialidad() { return especialidad; }
}
