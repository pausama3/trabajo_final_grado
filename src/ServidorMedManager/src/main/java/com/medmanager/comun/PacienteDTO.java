package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDate;

public class PacienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String dni;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String alergias;
    private boolean activo;

    public PacienteDTO(int id, String dni, String nombre, String apellidos, String telefono, 
                       String email, String direccion, LocalDate fechaNacimiento, 
                       String alergias, boolean activo) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.fechaNacimiento = fechaNacimiento;
        this.alergias = alergias;
        this.activo = activo;
    }

    public int getId() { return id; }
    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getDireccion() { return direccion; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getAlergias() { return alergias; }
    public boolean isActivo() { return activo; }
}