package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa a un trabajador de la clínica (tabla {@code trabajador}).
 * <p>
 * Contiene las credenciales de acceso (usuario y hash de la contraseña) y el rol,
 * que determina los permisos en el servidor. Estos datos nunca salen al cliente:
 * se envía un {@code TrabajadorDTO} con la información pública.
 */
@Entity // indica que es una entidad de la BD
@Table(name = "trabajador") // el nombre de la tabla en la BD
public class Trabajador {

	/** Roles posibles; se guardan en la BD como texto (EnumType.STRING). */
	public enum Rol {
	    RECEPCIONISTA, ENFERMERO, MEDICO
	}
	
	@Id // PK
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
	@Column(name = "id_trabajador")
	private int id;
	
	@Column(name = "dni", nullable = false, unique = true, length = 9)
	private String dni;
	
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;
	
	@Column(name = "apellidos", nullable = false, length = 100)
	private String apellidos;
	
	@Column(name = "telefono", nullable = true, length = 15)
	private String telefono;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "rol", nullable = false)
	private Rol rol;
	
	@Column(name = "usuario", nullable = false, unique = true, length = 50)
	private String usuario;
	
	@Column(name = "password_hash", nullable = false, length = 255)
	private String password_hash;   // nunca se guarda la contraseña en claro

	@ManyToOne   // solo los médicos tienen especialidad
	@JoinColumn(name = "id_especialidad", nullable = true)
	private Especialidad especialidad;

	// Constructores

	/** Constructor vacío exigido por JPA. */
	public Trabajador() {
	}

	/** Crea un trabajador nuevo (el id lo asigna la base de datos al guardarlo). */
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
	public int getId_trabajador() { return id; }
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