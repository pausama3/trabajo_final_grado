package com.medmanager.ServidorMedManager;

import jakarta.persistence.*;

@Entity // indica que es una entidad de la BD
@Table(name = "trabajador") // el nombre de la tabla en la BD
public class Trabajador {
	
	public enum Rol {
	    Recepcionista, Enfermero, Médico
	}
	
	@Id // PK
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
	@Column(name = "id_trabajador")
	private int id_trabajador;
	
	@Column(name = "dni", nullable = false, unique = true, length = 9)
	private String dni;
	
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;
	
	@Column(name = "apellidos", nullable = false, length = 100)
	private String apellidos;
	
	@Column(name = "telefono", nullable = true, length = 15)
	private String telefono;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "rol", nullable = false, columnDefinition = "ENUM('Recepcionista', 'Enfermero', 'Médico')")
	private Rol rol;
	
	@Column(name = "usuario", nullable = false, unique = true, length = 50)
	private String usuario;
	
	@Column(name = "password_hash", nullable = false, length = 255)
	private String password_hash; 

	@ManyToOne
	@JoinColumn(name = "id_especialidad", nullable = true)
	private Especialidad especialidad;
	
	// Constructores
	public Trabajador() {	
	}
	
	// Aquí cambiamos String rol por Rol rol
	public Trabajador(String dni, String nombre, String apellidos, String telefono, Rol rol, String usuario, String password_hash, Especialidad especialidad)  {
		this.dni = dni;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.telefono = telefono;
		this.rol = rol;
		this.usuario = usuario;
		this.password_hash = password_hash;
		this.especialidad = especialidad;
	}

	// Getters
	public int getId_trabajador() { return id_trabajador; }
	public String getDni() { return dni; }
	public String getNombre() { return nombre; }
	public String getApellidos() { return apellidos; }
	public String getTelefono() { return telefono; }
	public Rol getRol() { return rol; } // Cambiado de String a Rol
	public String getUsuario() { return usuario; }
	public String getPassword_hash() { return password_hash; }
	public Especialidad getEspecialidad() { return especialidad; }

	// Setters 
	public void setDni(String dni) { this.dni = dni; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setApellidos(String apellidos) { this.apellidos = apellidos; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
	public void setRol(Rol rol) { this.rol = rol; } // Cambiado de String a Rol
	public void setUsuario(String usuario) { this.usuario = usuario; }
	public void setPassword_hash(String password_hash) { this.password_hash = password_hash; }
	public void setEspecialidad(Especialidad especialidad) { this.especialidad = especialidad; }
}