package com.medmanager.ServidorMedManager.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.util.HibernateUtil;

public class PacienteDAO implements IDAO<Paciente> {

    /** Busca pacientes activos por DNI, nombre, apellidos o nombre completo (máximo 50). */
    public List<Paciente> buscar(String texto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Paciente p WHERE p.activo = true AND ("
                    + "p.dni LIKE :t OR p.nombre LIKE :t OR p.apellidos LIKE :t "
                    + "OR concat(p.nombre, ' ', p.apellidos) LIKE :t) "
                    + "ORDER BY p.apellidos, p.nombre", Paciente.class)
                    .setParameter("t", "%" + texto + "%")
                    .setMaxResults(50)
                    .list();
        }
    }

    @Override
    public void save(Paciente paciente) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(paciente);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;   // el error sube hasta ManejadorCliente, que avisará al cliente
        }
    }

    @Override
    public void update(Paciente paciente) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(paciente);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Paciente findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Paciente.class, id);
        }
    }

    @Override
    public List<Paciente> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Paciente ORDER BY apellidos", Paciente.class).list();
        }
    }
}