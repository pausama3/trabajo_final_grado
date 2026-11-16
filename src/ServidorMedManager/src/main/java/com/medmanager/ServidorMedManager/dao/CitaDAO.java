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

public class CitaDAO implements IDAO<Cita> {

    /** Citas de un médico en un día concreto, ordenadas por hora (incluye las canceladas). */
    public List<Cita> agendaDeMedico(int idTrabajador, LocalDate dia) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
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
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

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
            tx.commit();
            return cita;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /** Pasa a CANCELADA una cita PENDIENTE. Al cancelarla, MySQL libera el hueco (fecha_hora_activa = NULL). */
    public Cita cancelar(int idCita) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Cita cita = session.get(Cita.class, idCita);
            if (cita == null) {
                throw new IllegalArgumentException("La cita no existe");
            }
            if (!"PENDIENTE".equals(cita.getEstado())) {
                throw new IllegalStateException("Solo se pueden cancelar citas pendientes (esta está "
                        + cita.getEstado() + ")");
            }
            cita.setEstado("CANCELADA");
            tx.commit();
            return cita;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void save(Cita cita) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(cita);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void update(Cita cita) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(cita);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
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
