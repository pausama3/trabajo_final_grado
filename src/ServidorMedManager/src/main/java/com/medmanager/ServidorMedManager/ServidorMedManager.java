package com.medmanager.ServidorMedManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMedManager {
    
    private static final int PUERTO = 8080;

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor MedManager...");
        
        // Comprobamos la conexión a la base de datos de forma segura
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("✅ ¡Conexión a la base de datos MySQL establecida con éxito!");
            
        } catch (ExceptionInInitializerError e) {
            System.err.println("❌ ERROR CRÍTICO: No se pudo conectar a la base de datos MySQL.");
            System.err.println("Comprueba que MySQL está encendido y que el usuario/contraseña son correctos.");
            return; // Detenemos la ejecución aquí, ya que sin base de datos el servidor no puede funcionar
        }
        
        // Si la conexión tuvo éxito, abrimos el puerto para escuchar a los clientes
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            
            while (true) {
                Socket socketCliente = serverSocket.accept();
                System.out.println("¡Nuevo cliente conectado!: " + socketCliente.getInetAddress().getHostAddress());
                
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                new Thread(manejador).start();
            }
            
        } catch (IOException e) {
            System.err.println("Error al abrir el puerto del servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}