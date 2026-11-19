package com.medmanager.comun;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Objeto de transferencia (DTO) con los datos personales de un paciente.
 * <p>
 * Se usa tanto para mostrar resultados de búsqueda como para enviar altas
 * ({@code id = 0}) y modificaciones con la acción {@link Acciones#GUARDAR_PACIENTE}.
 */
public class PacienteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;                     // 0 si el paciente todavía no está dado de alta
    private String dni;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String email;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String alergias;
    private boolean activo;             // false si el paciente está dado de baja

    /**
     * Construye un paciente con todos sus datos.
     *
     * @param id              identificador (0 para un alta nueva)
     * @param dni             documento de identidad
     * @param nombre          nombre de pila
     * @param apellidos       apellidos
     * @param telefono        teléfono de contacto
     * @param email           correo electrónico
     * @param direccion       dirección postal
     * @param fechaNacimiento fecha de nacimiento
     * @param alergias        alergias conocidas
     * @param activo          si el paciente está activo en la clínica
     */
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

    // Getters
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