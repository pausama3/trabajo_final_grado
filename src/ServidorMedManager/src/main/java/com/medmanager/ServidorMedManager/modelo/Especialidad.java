package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;


@Entity //  indica que es una entidad de la BD
@Table(name = "especialidad")// el nombre de la tabla en la BD
public class Especialidad {
	
	@Id // PK
	@GeneratedValue(strategy = GenerationType.IDENTITY)//auto_increment
	@Column(name = "id")
	private int id;
	
	@Column(name = "nombre_especialidad", nullable = false, length = 50 )
	private String nombre_especialidad;
	
	@Column(name = "descripcion", nullable = true, length = 255 )
	private String descripcion;
	
	//constructores
	public Especialidad() {	
	}
	
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
