package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;


/**
 * Entidad JPA que representa una especialidad médica (tabla {@code especialidad}),
 * por ejemplo Medicina General o Pediatría. Cada médico puede tener una.
 */
@Entity //  indica que es una entidad de la BD
@Table(name = "especialidad")// el nombre de la tabla en la BD
public class Especialidad {

	@Id   // clave primaria autoincremental
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_especialidad")
	private int id;
	
	@Column(name = "nombre_especialidad", nullable = false, length = 50 )
	private String nombre_especialidad;
	
	@Column(name = "descripcion", nullable = true, length = 255 )
	private String descripcion;
	
	//constructores

	/** Constructor vacío exigido por JPA. */
	public Especialidad() {
	}

	/** Crea una especialidad nueva (el id lo asigna la base de datos). */
	public Especialidad(String nombre_especialidad, String descripcion) {
		this.nombre_especialidad = nombre_especialidad;
		this.descripcion = descripcion;
	}
	// getters
	public int getId() {
		return id;
	}
	public String getNombre_especialidad() {
		return nombre_especialidad;
	}
	public String getDescripcion() {
		return descripcion;
	}
	// Setters
	public void setNombre_especialidad(String nombre_especialidad) {
		this.nombre_especialidad = nombre_especialidad;
	}
	public void setDescripcion(String descripcion) { 
		this.descripcion = descripcion;
	}
}
