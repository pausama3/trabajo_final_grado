package com.medmanager.ServidorMedManager.dao;

import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.ServidorMedManager.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class TrabajadorDAO implements IDAO<Trabajador> {

    // Método específico para el Login seguro con BCrypt
    public Trabajador autenticar(String usuario, String passwordPlana) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Buscamos al trabajador por su nombre de usuario
            Query<Trabajador> query = session.createQuery("FROM Trabajador WHERE usuario = :user", Trabajador.class);
            query.setParameter("user", usuario);
            Trabajador trabajador = query.uniqueResult();
            
            // Si el trabajador existe, comprobamos si la contraseña coincide con el hash
            if (trabajador != null && BCrypt.checkpw(passwordPlana, trabajador.getPassword_hash())) {
                return trabajador; // Login exitoso (devuelve el objeto con su rol)
            }
            return null; // Login fallido (usuario no existe o clave incorrecta)
        } catch (Exception e) {
            System.err.println("Error al autenticar trabajador: " + e.getMessage());
            return null;
        }
    }

    /** Todos los médicos con su especialidad, ordenados por apellidos (para elegir médico al dar cita). */
    public List<Trabajador> listarMedicos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Trabajador t LEFT JOIN FETCH t.especialidad "
                    + "WHERE t.rol = :rol ORDER BY t.apellidos, t.nombre", Trabajador.class)
                    .setParameter("rol", Trabajador.Rol.MEDICO)
                    .list();
        }
    }

    @Override
    public void save(Trabajador entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    @Override
    public void update(Trabajador entity) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
    @Override
    public Trabajador findById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Trabajador.class, id);
        }
    }

    @Override
    public List<Trabajador> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Trabajador", Trabajador.class).list();
        }
    }
}
