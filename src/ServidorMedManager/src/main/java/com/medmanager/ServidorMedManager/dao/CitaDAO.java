package com.medmanager.ServidorMedManager.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.ServidorMedManager.util.HibernateUtil;

/**
 * Acceso a datos de las citas: agendas por día, creación, cancelación,
 * reprogramación y CRUD básico.
 * <p>
 * Las validaciones de negocio se hacen aquí dentro de la transacción; las de
 * concurrencia (huecos ocupados) las garantiza MySQL con restricciones UNIQUE.
 */
public class CitaDAO implements IDAO<Cita> {

    /** Citas de un médico en un día concreto, ordenadas por hora (incluye las canceladas). */
    public List<Cita> agendaDeMedico(int idTrabajador, LocalDate dia) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Intervalo [00:00 del día, 00:00 del día siguiente); JOIN FETCH evita cargas perezosas fuera de sesión
            return session.createQuery(
                    "FROM Cita c JOIN FETCH c.paciente "
                    + "WHERE c.trabajador.id = :med AND c.fecha_hora >= :ini AND c.fecha_hora < :fin "
                    + "ORDER BY c.fecha_hora", Cita.class)
                    .setParameter("med", idTrabajador)
                    .setParameter("ini", dia.atStartOfDay())
                    .setParameter("fin", dia.plusDays(1).atStartOfDay())
                    .list();
        }
    }

    /** Todas las citas de la clínica en un día (todos los médicos), ordenadas por hora y médico. */
    public List<Cita> agendaDelDia(LocalDate dia) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Cita c JOIN FETCH c.paciente JOIN FETCH c.trabajador t "
                    + "WHERE c.fecha_hora >= :ini AND c.fecha_hora < :fin "
                    + "ORDER BY c.fecha_hora, t.apellidos", Cita.class)
                    .setParameter("ini", dia.atStartOfDay())
                    .setParameter("fin", dia.plusDays(1).atStartOfDay())
                    .list();
        }
    }

    /**
     * Programa una cita PENDIENTE. Comprueba que el paciente está activo y que el trabajador es médico.
     * Los choques de horario (mismo médico o mismo paciente a la misma hora) los rechaza MySQL
     * con los UNIQUE de fecha_hora_activa: así es seguro aunque dos recepcionistas reserven a la vez.
     */
    public Cita crear(int idPaciente, int idMedico, LocalDateTime fechaHora, String motivo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {

                // Validaciones previas: paciente activo y trabajador con rol de médico
                Paciente paciente = session.get(Paciente.class, idPaciente);
                if (paciente == null || !paciente.isActivo()) {
                    throw new IllegalArgumentException("El paciente no existe o está dado de baja");
                }
                Trabajador medico = session.get(Trabajador.class, idMedico);
                if (medico == null || medico.getRol() != Trabajador.Rol.MEDICO) {
                    throw new IllegalArgumentException("El trabajador elegido no es un médico");
                }

                Cita cita = new Cita(paciente, medico, fechaHora, "PENDIENTE", motivo, null);
                session.persist(cita);
                tx.commit();   // aquí MySQL comprueba los UNIQUE de franja
                return cita;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    /** Pasa a CANCELADA una cita PENDIENTE. Al cancelarla, MySQL libera el hueco (fecha_hora_activa = NULL). */
    public Cita cancelar(int idCita) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {

                Cita cita = session.get(Cita.class, idCita);
                if (cita == null) {
                    throw new IllegalArgumentException("La cita no existe");
                }
                if (!"PENDIENTE".equals(cita.getEstado())) {
                    throw new IllegalStateException("Solo se pueden cancelar citas pendientes (esta está "
                            + cita.getEstado() + ")");
                }
                cita.setEstado("CANCELADA");
                tx.commit();   // entidad gestionada: Hibernate genera el UPDATE
                return cita;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    /**
     * Cambia la fecha/hora de una cita PENDIENTE (mismo paciente y mismo médico).
     * Igual que al crear, si el nuevo hueco ya está ocupado lo rechaza MySQL con los UNIQUE
     * de fecha_hora_activa, y el hueco antiguo queda libre al hacer el commit.
     */
    public Cita reprogramar(int idCita, LocalDateTime nuevaFechaHora) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {

                Cita cita = session.get(Cita.class, idCita);
                if (cita == null) {
                    throw new IllegalArgumentException("La cita no existe");
                }
                if (!"PENDIENTE".equals(cita.getEstado())) {
                    throw new IllegalStateException("Solo se pueden reprogramar citas pendientes (esta está "
                            + cita.getEstado() + ")");
                }
                if (!cita.getPaciente().isActivo()) {
                    throw new IllegalStateException("El paciente está dado de baja");
                }
                if (nuevaFechaHora.equals(cita.getFecha_hora())) {   // no tiene sentido "mover" al mismo hueco
                    throw new IllegalArgumentException("La nueva fecha y hora es la misma que la actual");
                }
                cita.setFecha_hora(nuevaFechaHora);
                tx.commit();
                return cita;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    // ---- Operaciones CRUD de IDAO ----

    /** Inserta una cita tal cual, sin las validaciones de {@link #crear}. */
    @Override
    public void save(Cita cita) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(cita);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    /** Sobrescribe todos los campos de la cita con los de la entidad recibida. */
    @Override
    public void update(Cita cita) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(cita);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    @Override
    public Cita findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Cita.class, id);
        }
    }

    @Override
    public List<Cita> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Cita ORDER BY fecha_hora", Cita.class).list();
        }
    }
}
