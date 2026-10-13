package com.medmanager.ServidorMedManager;

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
            out.writeUTF("Conexión establecida con el servidor MedManager.");
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