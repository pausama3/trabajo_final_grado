package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;
import java.time.LocalDate; // Importamos LocalDate para fechas sin hora

@Entity // indica que es una entidad de la BD
@Table(name = "paciente") // el nombre de la tabla en la BD
public class Paciente {

	@Id // PK
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
	@Column(name= "id_paciente")
	private int id_paciente;
	
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
	
	@Column(name = "direccion", nullable = true, length = 50)
	private String direccion;
	
	@Column(name = "fecha_nacimiento", nullable = true)
	private LocalDate fecha_nacimiento; // Usamos LocalDate
	
	@Column(name = "activo", nullable = false, columnDefinition = "boolean default true")
	private boolean activo = true; 
	
	// Constructores
	public Paciente() {	
	}
	
	public Paciente(String dni, LocalDate fecha_nacimiento, String nombre, String apellidos, String telefono, String email, String direccion, boolean activo) {
		this.dni = dni;
		this.fecha_nacimiento = fecha_nacimiento;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.telefono = telefono;
		this.email = email;
		this.direccion = direccion;
		this.activo = activo;
	}

	// Getters
	public int getId_paciente() { return id_paciente; }
	public String getDni() { return dni; }
	public LocalDate getFecha_nacimiento() { return fecha_nacimiento; }
	public String getNombre() { return nombre; }
	public String getApellidos() { return apellidos; }
	public String getTelefono() { return telefono; }
	public String getEmail() { return email; }
	public String getDireccion() { return direccion; }
	public boolean isActivo() { return activo; } 

	// Setters
	public void setDni(String dni) { this.dni = dni; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setFecha_nacimiento(LocalDate fecha_nacimiento) { this.fecha_nacimiento = fecha_nacimiento; }
	public void setApellidos(String apellidos) { this.apellidos = apellidos; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
	public void setEmail(String email) { this.email = email; }
	public void setDireccion(String direccion) { this.direccion = direccion; }
	public void setActivo(boolean activo) { this.activo = activo; }
}