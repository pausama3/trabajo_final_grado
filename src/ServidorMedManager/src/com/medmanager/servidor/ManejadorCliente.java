package com.medmanager.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    
    private Socket socket;

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            // 1. Enviamos un mensaje de confirmación al cliente
            out.writeUTF("Conexión establecida con el servidor MedManager.");
            
            // 2. Leemos lo que nos envía el cliente
            String mensaje = in.readUTF();
            System.out.println("Mensaje recibido del cliente: " + mensaje);
            
        } catch (IOException e) {
            System.err.println("Error en la comunicación con el cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Conexión cerrada.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}