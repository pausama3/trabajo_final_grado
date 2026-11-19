package com.medmanager.ServidorMedManager.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una entrada del historial médico (tabla {@code historial_medico}).
 * <p>
 * Cada entrada es el informe que redacta el médico al cerrar una cita, por lo
 * que la relación con {@link Cita} es uno a uno: una cita tiene como máximo un informe.
 */
@Entity // indica que es una entidad de la BD
@Table(name = "historial_medico") // el nombre exacto de la tabla en la BD
public class Historial_Medico {

	@Id // PK (PRI)
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
	@Column(name = "id_historial")
	private int id_historial;

	@ManyToOne   // un paciente acumula muchos informes a lo largo del tiempo
	@JoinColumn(name = "id_paciente", nullable = false)
	private Paciente paciente;

	@OneToOne   // un informe por cita (restricción UNIQUE en la BD)
	@JoinColumn(name = "id_cita", nullable = false, unique = true)
	private Cita cita;

	// Type: text | Null: NO
	@Column(name = "diagnostico", nullable = false, columnDefinition = "TEXT")
	private String diagnostico;

	// Type: text | Null: YES
	@Column(name = "tratamiento", nullable = true, columnDefinition = "TEXT")
	private String tratamiento;

	// Type: datetime | Null: YES | Default: CURRENT_TIMESTAMP
	@Column(name = "fecha_registro", nullable = true, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime fecha_registro = LocalDateTime.now();
	
	@Column(name = "activo", nullable = false)
	private boolean activo = true;   // borrado lógico: el historial nunca se elimina físicamente

	// Constructores

	/** Constructor vacío exigido por JPA. */
	public Historial_Medico() {
	}

	/** Crea un informe con fecha de registro igual al momento actual. */
	public Historial_Medico(Paciente paciente, Cita cita, String diagnostico, String tratamiento, boolean activo) {
		this.paciente = paciente;
		this.cita = cita;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.fecha_registro = LocalDateTime.now();
		this.activo = activo;
	}

	/** Crea un informe con una fecha de registro concreta (p. ej., para cargar datos históricos). */
	public Historial_Medico(Paciente paciente, Cita cita, String diagnostico, String tratamiento, LocalDateTime fecha_registro,  boolean activo) {
		this.paciente = paciente;
		this.cita = cita;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.fecha_registro = fecha_registro;
		this.activo = activo;
	}

	// Getters
	public int getId_historial() { return id_historial; }
	public Paciente getPaciente() { return paciente; }
	public Cita getCita() { return cita; }
	public String getDiagnostico() { return diagnostico; }
	public String getTratamiento() { return tratamiento; }
	public LocalDateTime getFecha_registro() { return fecha_registro; }
	public boolean isActivo() { return activo; }

	// Setters 
	public void setPaciente(Paciente paciente) { this.paciente = paciente; }
	public void setCita(Cita cita) { this.cita = cita; }
	public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
	public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
	public void setFecha_registro(LocalDateTime fecha_registro) { this.fecha_registro = fecha_registro; }
	public void setActivo(boolean activo) { this.activo = activo; }
}