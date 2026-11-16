package com.medmanager.ServidorMedManager.dao;

import java.time.LocalDate;
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

    /**
     * Cambia los datos personales de un paciente. NO toca las alergias (las gestiona el médico, RN3)
     * ni el estado activo (para eso está darDeBaja).
     */
    public Paciente modificarDatos(int id, String dni, String nombre, String apellidos, String telefono,
                                   String email, String direccion, LocalDate fechaNacimiento) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Paciente p = session.get(Paciente.class, id);
            if (p == null || !p.isActivo()) {
                throw new IllegalArgumentException("El paciente no existe o está dado de baja");
            }
            p.setDni(dni);
            p.setNombre(nombre);
            p.setApellidos(apellidos);
            p.setTelefono(telefono);
            p.setEmail(email);
            p.setDireccion(direccion);
            p.setFecha_nacimiento(fechaNacimiento);
            tx.commit();
            return p;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /**
     * Baja lógica: el paciente queda inactivo (no se borra, su historial se conserva) y sus citas
     * PENDIENTES de hoy en adelante se cancelan para liberar los huecos.
     *
     * @return número de citas canceladas
     */
    public int darDeBaja(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Paciente p = session.get(Paciente.class, id);
            if (p == null || !p.isActivo()) {
                throw new IllegalArgumentException("El paciente no existe o ya está dado de baja");
            }
            p.setActivo(false);
            int canceladas = session.createMutationQuery(
                    "UPDATE Cita c SET c.estado = 'CANCELADA' "
                    + "WHERE c.paciente.id = :id AND c.estado = 'PENDIENTE' AND c.fecha_hora >= :hoy")
                    .setParameter("id", id)
                    .setParameter("hoy", LocalDate.now().atStartOfDay())
                    .executeUpdate();
            tx.commit();
            return canceladas;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
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