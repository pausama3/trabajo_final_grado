package com.medmanager.comun;

import java.io.Serializable;

/**
 * Objeto de transferencia (DTO) con los datos públicos de un trabajador de la
 * clínica. Nunca incluye credenciales.
 * <p>
 * Lo devuelve el servidor tras un login correcto (usuario en sesión) y al
 * listar médicos para asignar citas.
 */
public class TrabajadorDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String nombre;
    private final String apellidos;
    private final String rol;            // determina qué panel y qué permisos tiene
    private final String especialidad;   // solo médicos; null para el resto

    /**
     * Construye un trabajador sin especialidad (personal no médico).
     *
     * @param id        identificador del trabajador
     * @param nombre    nombre de pila
     * @param apellidos apellidos
     * @param rol       rol dentro de la clínica
     */
    public TrabajadorDTO(int id, String nombre, String apellidos, String rol) {
        this(id, nombre, apellidos, rol, null);
    }

    /**
     * Construye un trabajador con todos sus datos.
     *
     * @param id           identificador del trabajador
     * @param nombre       nombre de pila
     * @param apellidos    apellidos
     * @param rol          rol dentro de la clínica
     * @param especialidad especialidad médica (null si no es médico)
     */
    public TrabajadorDTO(int id, String nombre, String apellidos, String rol, String especialidad) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.rol = rol;
        this.especialidad = especialidad;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getRol() { return rol; }
    public String getEspecialidad() { return especialidad; }
}
