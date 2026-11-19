package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;
import java.time.LocalDate; // Importamos LocalDate para fechas sin hora

/**
 * Entidad JPA que representa a un paciente de la clínica (tabla {@code paciente}).
 * <p>
 * Los pacientes no se borran: al darlos de baja se marca {@code activo = false}
 * para conservar su historial médico.
 */
@Entity // indica que es una entidad de la BD
@Table(name = "paciente") // el nombre de la tabla en la BD
public class Paciente {

	@Id   // clave primaria autoincremental
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_paciente")
	private int id;
	
	@Column(name = "dni", nullable = false, length = 9)
	private String dni;
	
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;
	
	@Column(name = "apellidos", nullable = false, length = 100)
	private String apellidos;
	
	@Column(name = "telefono", nullable = true, length = 15)
	private String telefono;
	
	@Column(name = "email", nullable = true, length = 100)
	private String email;
	
	@Column(name = "direccion", nullable = true, length = 255)
	private String direccion;
	
	@Column(name = "fecha_nacimiento", nullable = true)
	private LocalDate fecha_nacimiento; // Usamos LocalDate
	
	@Column(name = "alergias", nullable = true, length = 255)
	private String alergias;   // dato clínico: solo visible para personal sanitario (RN3)

	@Column(name = "activo", nullable = false)
	private boolean activo = true;   // borrado lógico

	// Constructores

	/** Constructor vacío exigido por JPA. */
	public Paciente() {
	}

	/** Crea un paciente nuevo (el id lo asigna la base de datos al guardarlo). */
	public Paciente(String dni, LocalDate fecha_nacimiento, String nombre, String apellidos, String telefono, String email, String direccion, String alergias, boolean activo) {
		this.dni = dni;
		this.fecha_nacimiento = fecha_nacimiento;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.telefono = telefono;
		this.email = email;
		this.direccion = direccion;
		this.alergias = alergias;
		this.activo = activo;
	}

	// Getters
	public int getId_paciente() { return id; }
	public String getDni() { return dni; }
	public LocalDate getFecha_nacimiento() { return fecha_nacimiento; }
	public String getNombre() { return nombre; }
	public String getApellidos() { return apellidos; }
	public String getTelefono() { return telefono; }
	public String getEmail() { return email; }
	public String getDireccion() { return direccion; }
	public String getAlergias() { return alergias; }
	public boolean isActivo() { return activo; } 

	// Setters
	public void setDni(String dni) { this.dni = dni; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setFecha_nacimiento(LocalDate fecha_nacimiento) { this.fecha_nacimiento = fecha_nacimiento; }
	public void setApellidos(String apellidos) { this.apellidos = apellidos; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
	public void setEmail(String email) { this.email = email; }
	public void setDireccion(String direccion) { this.direccion = direccion; }
	public void setAlergias (String alergias) { this.alergias = alergias; }
	public void setActivo(boolean activo) { this.activo = activo; }
}