package com.medmanager.ServidorMedManager;

import jakarta.persistence.*;


@Entity //  indica que es una entidad de la BD
@Table(name = "especialidad")// el nombre de la tabla en la BD
public class Especialidad {
	
	@Id // PK
	@GeneratedValue(strategy = GenerationType.IDENTITY)//auto_increment
	@Column(name = "id_especialidad")
	private int id_especialidad;
	
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
	public int getId_especialidad() {
		return id_especialidad;
	}
	public String getnombre_especialidad() {
		return nombre_especialidad;
	}
	public String getdescripcion() {
		return descripcion;
	}
	// Setters
	/*public void setId_especialidad(int id_especialidad) {
		this.id_especialidad = id_especialidad;
	}*/
	public void setnombre_especialidad(String nombre_especialidad) {
		this.nombre_especialidad = nombre_especialidad;
	}
	public void setdescripcion(String descripcion) { 
		this.descripcion = descripcion;
	}
}
