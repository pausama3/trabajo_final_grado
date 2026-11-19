package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una cita médica (tabla {@code cita}).
 * <p>
 * Relaciona un paciente con el médico que lo atiende en una fecha y hora
 * concretas. El estado indica su ciclo de vida: PENDIENTE, REALIZADA o CANCELADA.
 */
@Entity
@Table(name = "cita")
public class Cita {

	@Id   // clave primaria autoincremental
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_cita")
	private int id;

	// --- CLAVES FORÁNEAS COMO OBJETOS ---
	@ManyToOne   // un paciente puede tener muchas citas
	@JoinColumn(name = "id_paciente", nullable = false)
	private Paciente paciente;

	@ManyToOne   // un médico atiende muchas citas
	@JoinColumn(name = "id_trabajador", nullable = false)
	private Trabajador trabajador;
	// ------------------------------------

	@Column(name = "fecha_hora", nullable = false)
	private LocalDateTime fecha_hora;   // en franjas de 15 minutos (lo garantiza la BD)

	@Column (name = "estado", nullable = false, length = 20)
	private String estado;              // PENDIENTE, REALIZADA o CANCELADA

	@Column(name = "motivo_consulta", nullable = true, length = 255)
	private String motivo_consulta;

	@Column(name = "observaciones_enfermeria", nullable = true, columnDefinition = "TEXT")
	private String observaciones_enfermeria;

	// Constructores

	/** Constructor vacío exigido por JPA. */
	public Cita() {
	}

	/**
	 * Crea una cita nueva (el id lo asigna la base de datos al guardarla).
	 */
	public Cita(Paciente paciente, Trabajador trabajador, LocalDateTime fecha_hora, String estado, String motivo_consulta, String observaciones_enfermeria) {
		this.paciente = paciente;
		this.trabajador = trabajador;
		this.fecha_hora = fecha_hora;
		this.estado = estado;
		this.motivo_consulta = motivo_consulta;
		this.observaciones_enfermeria = observaciones_enfermeria;
	}

	// Getters
	public int getId_cita() {
		return id;
	}

	public Paciente getPaciente() {
		return paciente;
	}

	public Trabajador getTrabajador() {
		return trabajador;
	}

	public LocalDateTime getFecha_hora() {
		return fecha_hora;
	}

	public String getEstado() {
		return estado;
	}

	public String getMotivo_consulta() {
		return motivo_consulta;
	}

	/** @return las observaciones de enfermería (dato clínico, oculto a recepción por RN3) */
	public String getObservaciones() {
		return observaciones_enfermeria;
	}

	// Setters
	public void setPaciente(Paciente paciente) {
		this.paciente = paciente;
	}

	public void setTrabajador(Trabajador trabajador) {
		this.trabajador = trabajador;
	}

	public void setFecha_hora(LocalDateTime fecha_hora) {
		this.fecha_hora = fecha_hora;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public void setMotivo_consulta(String motivo_consulta) {
		this.motivo_consulta = motivo_consulta;
	}

	public void setObservaciones_enfermeria(String observaciones_enfermeria) {
		this.observaciones_enfermeria = observaciones_enfermeria;
	}

}