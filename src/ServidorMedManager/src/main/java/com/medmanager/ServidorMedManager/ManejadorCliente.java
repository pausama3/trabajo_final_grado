package com.medmanager.ServidorMedManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.medmanager.ServidorMedManager.dao.TrabajadorDAO;
import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.comun.Acciones;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private Trabajador sesion;   // null = nadie ha iniciado sesión en esta conexión

    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            out.flush();   // envía la cabecera para que el cliente pueda abrir su ObjectInputStream

            while (true) {   // una conexión, muchas peticiones
                Object recibido = in.readObject();

                if (!(recibido instanceof PaqueteRed)) {
                    responder(out, Acciones.ERROR, "Mensaje no válido");
                    continue;
                }
                PaqueteRed paquete = (PaqueteRed) recibido;
                System.out.println("Acción recibida: " + paquete.getAccion());

                switch (paquete.getAccion()) {
                    case Acciones.LOGIN:
                        procesarLogin(paquete, out);
                        break;
                    default:
                        responder(out, Acciones.ERROR, "Acción desconocida: " + paquete.getAccion());
                }
            }

        } catch (EOFException e) {
            System.out.println("Cliente desconectado.");
        } catch (Exception e) {
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

    private void procesarLogin(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof LoginDTO)) {
            responder(out, Acciones.ERROR, "Datos de login no válidos");
            return;
        }
        LoginDTO login = (LoginDTO) paquete.getPayload();
        Trabajador trabajador = trabajadorDAO.autenticar(login.getUsuario(), login.getPassword());

        if (trabajador == null) {
            sesion = null;
            responder(out, Acciones.ERROR, "Usuario o contraseña incorrectos");
            return;
        }
        sesion = trabajador;   // el servidor recuerda quién es y qué rol tiene
        TrabajadorDTO dto = new TrabajadorDTO(
            trabajador.getId_trabajador(), trabajador.getNombre(),
            trabajador.getApellidos(), trabajador.getRol().name());
        responder(out, Acciones.OK, dto);
    }

    private void responder(ObjectOutputStream out, String accion, Object payload) throws IOException {
        out.writeObject(new PaqueteRed(accion, payload));
        out.flush();
    }
}