package com.medmanager.ServidorMedManager.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Historial_Medico;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.util.HibernateUtil;

/**
 * Acceso a datos del historial médico: búsqueda de informes, registro de
 * consultas y CRUD básico.
 */
public class HistorialMedicoDAO implements IDAO<Historial_Medico> {

    /**
     * Informes activos de los pacientes cuyo DNI, nombre, apellidos o nombre completo contienen el texto.
     * Ordenados por paciente y, dentro de cada uno, del más reciente al más antiguo (máximo 200).
     */
    public List<Historial_Medico> buscarPorPaciente(String texto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Historial_Medico h JOIN FETCH h.paciente p "
                    + "JOIN FETCH h.cita c JOIN FETCH c.trabajador "   // el médico, para mostrar quién lo redactó
                    + "WHERE h.activo = true AND ("
                    + "p.dni LIKE :t OR p.nombre LIKE :t OR p.apellidos LIKE :t "
                    + "OR concat(p.nombre, ' ', p.apellidos) LIKE :t) "
                    + "ORDER BY p.apellidos, p.nombre, h.fecha_registro DESC", Historial_Medico.class)
                    .setParameter("t", "%" + texto + "%")
                    .setMaxResults(200)
                    .list();
        }
    }

    /**
     * Cierra una consulta en UNA transacción: crea el informe, marca la cita como REALIZADA
     * y actualiza las alergias del paciente. Si algo falla, no se guarda nada.
     *
     * @throws IllegalArgumentException si la cita no existe o no es de este médico
     * @throws IllegalStateException    si la cita ya no está PENDIENTE
     */
    public Historial_Medico registrarConsulta(int idCita, int idMedico, String diagnostico,
                                              String tratamiento, String alergias) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {

                // Un médico solo puede cerrar sus propias citas
                Cita cita = session.get(Cita.class, idCita);
                if (cita == null || cita.getTrabajador().getId_trabajador() != idMedico) {
                    throw new IllegalArgumentException("La cita no existe o no está asignada a ti");
                }
                if (!"PENDIENTE".equals(cita.getEstado())) {
                    throw new IllegalStateException("La cita ya está " + cita.getEstado());
                }

                Paciente paciente = cita.getPaciente();
                paciente.setAlergias(alergias);
                cita.setEstado("REALIZADA");

                Historial_Medico informe = new Historial_Medico(paciente, cita, diagnostico, tratamiento, true);
                session.persist(informe);   // cita y paciente se guardan solos: ya están en la sesión

                tx.commit();
                return informe;
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    // ---- Operaciones CRUD de IDAO ----

    /** Inserta un informe tal cual, sin las validaciones de {@link #registrarConsulta}. */
    @Override
    public void save(Historial_Medico historial) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(historial);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    @Override
    public void update(Historial_Medico historial) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.merge(historial);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();   // antes de que se cierre la sesión
                throw e;
            }
        }
    }

    @Override
    public Historial_Medico findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Historial_Medico.class, id);
        }
    }

    /** Todos los informes activos (los desactivados se excluyen). */
    @Override
    public List<Historial_Medico> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Historial_Medico WHERE activo = true", Historial_Medico.class).list();
        }
    }
}
