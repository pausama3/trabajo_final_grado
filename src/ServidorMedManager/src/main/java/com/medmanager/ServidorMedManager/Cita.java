package com.medmanager.ServidorMedManager;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cita")
public class Cita {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_cita")
	private int id_cita;
	
	// --- CLAVES FORÁNEAS COMO OBJETOS ---
	@ManyToOne
	@JoinColumn(name = "id_paciente", nullable = false)
	private Paciente paciente;
	
	@ManyToOne
	@JoinColumn(name = "id_trabajador", nullable = false)
	private Trabajador trabajador;
	// ------------------------------------
	
	@Column(name = "fecha_hora", nullable = false)
	private LocalDateTime fecha_hora;
	
	@Column (name = "estado", nullable = false, length = 20)
	private String estado;
	
	@Column(name = "motivo_consulta", nullable = true, length = 255)
	private String motivo_consulta;
	
	@Column(name = "diagnostico", nullable = true)
	private String diagnostico;
	
	// Constructores
	public Cita() {	
	}
	
	// Fíjate que ahora el constructor pide objetos Paciente y Trabajador, no números int
	public Cita(Paciente paciente, Trabajador trabajador, LocalDateTime fecha_hora, String estado, String motivo_consulta, String diagnostico) {
		this.paciente = paciente;
		this.trabajador = trabajador;
		this.fecha_hora = fecha_hora;
		this.estado = estado;
		this.motivo_consulta = motivo_consulta;
		this.diagnostico = diagnostico;
	}

	// Getters
	public int getId_cita() { return id_cita; }
	public Paciente getPaciente() { return paciente; }
	public Trabajador getTrabajador() { return trabajador; }
	public LocalDateTime getFecha_hora() { return fecha_hora; }
	public String getEstado() { return estado; }
	public String getMotivo_consulta() { return motivo_consulta; }
	public String getDiagnostico() { return diagnostico; }

	// Setters (sin el setId_cita)
	public void setPaciente(Paciente paciente) { this.paciente = paciente; }
	public void setTrabajador(Trabajador trabajador) { this.trabajador = trabajador; }
	public void setFecha_hora(LocalDateTime fecha_hora) { this.fecha_hora = fecha_hora; }
	public void setEstado(String estado) { this.estado = estado; }
	public void setMotivo_consulta(String motivo_consulta) { this.motivo_consulta = motivo_consulta; }
	public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }
}