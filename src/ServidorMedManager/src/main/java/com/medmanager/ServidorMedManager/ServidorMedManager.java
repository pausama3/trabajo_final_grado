package com.medmanager.ServidorMedManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.medmanager.ServidorMedManager.util.HibernateUtil;

/**
 * Punto de entrada del servidor de MedManager.
 * <p>
 * Comprueba la conexión con la base de datos y, si es correcta, queda a la
 * escucha de clientes en un puerto TCP. Cada cliente se atiende en su propio
 * hilo mediante un {@link ManejadorCliente}.
 */
public class ServidorMedManager {

    /** Puerto TCP en el que escucha el servidor (debe coincidir con el del cliente). */
    private static final int PUERTO = 8080;

    /**
     * Arranca el servidor.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        System.out.println("Iniciando Servidor MedManager...");

        // Comprobamos la conexión a la base de datos de forma segura
        try {
            HibernateUtil.getSessionFactory();   // fuerza la creación de la SessionFactory
            System.out.println("✅ ¡Conexión a la base de datos MySQL establecida con éxito!");

        } catch (ExceptionInInitializerError e) {
            System.err.println("❌ ERROR CRÍTICO: No se pudo conectar a la base de datos MySQL.");
            System.err.println("Comprueba que MySQL está encendido y que el usuario/contraseña son correctos.");
            return; // Detenemos la ejecución aquí, ya que sin base de datos el servidor no puede funcionar
        }

        // Si la conexión tuvo éxito, abrimos el puerto para escuchar a los clientes
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            // Bucle principal: acepta conexiones indefinidamente
            while (true) {
                Socket socketCliente = serverSocket.accept();   // bloquea hasta que llega un cliente
                System.out.println("¡Nuevo cliente conectado!: " + socketCliente.getInetAddress().getHostAddress());

                // Un hilo por cliente: así varios usuarios pueden trabajar a la vez
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                new Thread(manejador).start();
            }

        } catch (IOException e) {
            System.err.println("Error al abrir el puerto del servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}