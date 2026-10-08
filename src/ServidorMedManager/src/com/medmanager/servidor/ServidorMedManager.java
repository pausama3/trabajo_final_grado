package com.medmanager.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorMedManager {
    
    private static final int PUERTO = 8080;

    public static void main(String[] args) {
        System.out.println("Iniciando Servidor MedManager...");
        
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);
            
            while (true) {
                // El servidor se queda pausado aquí hasta que alguien se conecta
                Socket socketCliente = serverSocket.accept();
                System.out.println("¡Nuevo cliente conectado!: " + socketCliente.getInetAddress().getHostAddress());
                
                // Pasamos el cliente a un nuevo hilo para no bloquear el servidor
                ManejadorCliente manejador = new ManejadorCliente(socketCliente);
                new Thread(manejador).start();
            }
            
        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}