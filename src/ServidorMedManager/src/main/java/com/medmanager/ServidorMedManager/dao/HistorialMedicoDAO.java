package com.medmanager.ServidorMedManager.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Historial_Medico;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.util.HibernateUtil;

public class HistorialMedicoDAO implements IDAO<Historial_Medico> {

    /** Informes activos de las citas atendidas por un médico, del más reciente al más antiguo. */
    public List<Historial_Medico> informesDeMedico(int idTrabajador) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Historial_Medico h JOIN FETCH h.paciente JOIN FETCH h.cita c "
                    + "WHERE h.activo = true AND c.trabajador.id = :med "
                    + "ORDER BY h.fecha_registro DESC", Historial_Medico.class)
                    .setParameter("med", idTrabajador)
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
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

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
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void save(Historial_Medico historial) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(historial);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public void update(Historial_Medico historial) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(historial);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) tx.rollback();
            throw e;
        }
    }

    @Override
    public Historial_Medico findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Historial_Medico.class, id);
        }
    }

    @Override
    public List<Historial_Medico> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Historial_Medico WHERE activo = true", Historial_Medico.class).list();
        }
    }
}
