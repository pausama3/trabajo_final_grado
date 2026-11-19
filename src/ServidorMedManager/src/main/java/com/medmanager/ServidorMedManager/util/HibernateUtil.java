package com.medmanager.ServidorMedManager.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Proporciona la {@link SessionFactory} única de Hibernate para todo el servidor.
 * <p>
 * Crear una SessionFactory es costoso, por lo que se construye una sola vez al
 * cargar la clase a partir de {@code hibernate.cfg.xml} y se comparte entre
 * todos los hilos (es thread-safe). Cada operación abre su propia sesión.
 */
public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Lee la configuración y crea la SessionFactory.
     *
     * @throws ExceptionInInitializerError si no se puede configurar o conectar con la base de datos
     */
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Fallo al inicializar la SessionFactory de Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * @return la SessionFactory compartida
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /** Cierra la SessionFactory y libera las conexiones con la base de datos. */
    public static void shutdown() {
        getSessionFactory().close();
    }
}