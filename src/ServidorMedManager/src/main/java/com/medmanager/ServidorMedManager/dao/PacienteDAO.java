package com.medmanager.ServidorMedManager.dao;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.util.HibernateUtil;

/**
 * Acceso a datos de los pacientes: búsqueda, modificación, baja lógica y CRUD básico.
 * <p>
 * Las operaciones de escritura se ejecutan en una transacción; si algo falla se
 * deshace y la excepción se propaga para que {@code ManejadorCliente} informe al cliente.
 */
public class PacienteDAO implements IDAO<Paciente> {

    /** Busca pacientes activos por DNI, nombre, apellidos o nombre completo (máximo 50). */
    public List<Paciente> buscar(String texto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Búsqueda parcial ("contiene"); el parámetro evita la inyección SQL
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
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
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
                tx.commit();   // la entidad está gestionada: Hibernate genera el UPDATE automáticamente
                return p;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    /**
     * Baja lógica: el paciente queda inactivo (no se borra, su historial se conserva) y sus citas
     * PENDIENTES de hoy en adelante se cancelan para liberar los huecos.
     *
     * @return número de citas canceladas
     */
    public int darDeBaja(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Paciente p = session.get(Paciente.class, id);
                if (p == null || !p.isActivo()) {
                    throw new IllegalArgumentException("El paciente no existe o ya está dado de baja");
                }
                p.setActivo(false);
                // Actualización masiva en la misma transacción: o se hace todo o nada
                int canceladas = session.createMutationQuery(
                        "UPDATE Cita c SET c.estado = 'CANCELADA' "
                        + "WHERE c.paciente.id = :id AND c.estado = 'PENDIENTE' AND c.fecha_hora >= :hoy")
                        .setParameter("id", id)
                        .setParameter("hoy", LocalDate.now().atStartOfDay())
                        .executeUpdate();
                tx.commit();
                return canceladas;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    // ---- Operaciones CRUD de IDAO ----

    /** Inserta un paciente nuevo. */
    @Override
    public void save(Paciente paciente) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(paciente);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;   // el error sube hasta ManejadorCliente, que avisará al cliente
            }
        }
    }

    /** Sobrescribe todos los campos del paciente con los de la entidad recibida. */
    @Override
    public void update(Paciente paciente) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(paciente);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    @Override
    public Paciente findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Paciente.class, id);
        }
    }

    /** Todos los pacientes (activos e inactivos) ordenados por apellidos. */
    @Override
    public List<Paciente> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Paciente ORDER BY apellidos", Paciente.class).list();
        }
    }
}