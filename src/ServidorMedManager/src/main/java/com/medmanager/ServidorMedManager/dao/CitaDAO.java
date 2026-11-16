package com.medmanager.ServidorMedManager.dao;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Cita;
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
