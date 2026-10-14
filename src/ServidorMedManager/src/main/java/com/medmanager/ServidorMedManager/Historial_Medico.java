package com.medmanager.ServidorMedManager;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity // indica que es una entidad de la BD
@Table(name = "historial_medico") // el nombre exacto de la tabla en la BD
public class Historial_Medico {

	@Id // PK (PRI)
	@GeneratedValue(strategy = GenerationType.IDENTITY) // auto_increment
	@Column(name = "id_historial")
	private int id_historial;

	// Key: MUL | Null: NO
	@ManyToOne
	@JoinColumn(name = "id_paciente", nullable = false)
	private Paciente paciente;

	// Key: MUL | Null: NO (Cambiado a @ManyToOne para coincidir con el MUL de tu tabla)
	@ManyToOne
	@JoinColumn(name = "id_cita", nullable = false)
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

	// Constructores
	public Historial_Medico() {
	}

	public Historial_Medico(Paciente paciente, Cita cita, String diagnostico, String tratamiento) {
		this.paciente = paciente;
		this.cita = cita;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.fecha_registro = LocalDateTime.now();
	}

	public Historial_Medico(Paciente paciente, Cita cita, String diagnostico, String tratamiento, LocalDateTime fecha_registro) {
		this.paciente = paciente;
		this.cita = cita;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.fecha_registro = fecha_registro;
	}

	// Getters
	public int getId_historial() { return id_historial; }
	public Paciente getPaciente() { return paciente; }
	public Cita getCita() { return cita; }
	public String getDiagnostico() { return diagnostico; }
	public String getTratamiento() { return tratamiento; }
	public LocalDateTime getFecha_registro() { return fecha_registro; }

	// Setters (sin setId_historial porque es auto_increment)
	public void setPaciente(Paciente paciente) { this.paciente = paciente; }
	public void setCita(Cita cita) { this.cita = cita; }
	public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
	public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
	public void setFecha_registro(LocalDateTime fecha_registro) { this.fecha_registro = fecha_registro; }
}