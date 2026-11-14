package com.medmanager.ServidorMedManager.util;

import java.io.*;
import java.net.Socket;
import com.medmanager.comun.*;

public class ClientePrueba {
    public static void main(String[] args) throws Exception {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            login(out, in, "recepcion1", "Recep123!");   // correcto
            login(out, in, "recepcion1", "mala");        // incorrecto
            login(out, in, "medico1", "Medico123!");     // correcto
            out.writeObject(new PaqueteRed("INVENTADA"));
            out.flush();
            PaqueteRed r = (PaqueteRed) in.readObject();
            System.out.println("INVENTADA -> " + r.getAccion() + ": " + r.getPayload());
        }
    }

    private static void login(ObjectOutputStream out, ObjectInputStream in,
                              String usuario, String clave) throws Exception {
        out.writeObject(new PaqueteRed(Acciones.LOGIN, new LoginDTO(usuario, clave)));
        out.flush();
        PaqueteRed r = (PaqueteRed) in.readObject();
        if (Acciones.OK.equals(r.getAccion())) {
            TrabajadorDTO t = (TrabajadorDTO) r.getPayload();
            System.out.println(usuario + " -> OK, rol " + t.getRol() + ", " + t.getNombre());
        } else {
            System.out.println(usuario + " -> " + r.getAccion() + ": " + r.getPayload());
        }
    }
}