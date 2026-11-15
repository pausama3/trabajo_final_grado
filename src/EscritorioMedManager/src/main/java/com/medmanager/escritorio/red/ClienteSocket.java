package com.medmanager.escritorio.red;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

/** Singleton: una única conexión con el servidor para toda la aplicación. */
public class ClienteSocket {

    private static final ClienteSocket INSTANCIA = new ClienteSocket();

    public static ClienteSocket getInstance() {
        return INSTANCIA;
    }

    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;
    private static final int TIMEOUT_CONEXION_MS = 3000;
    private static final int TIMEOUT_RESPUESTA_MS = 10000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private TrabajadorDTO usuarioActual;   // quién ha iniciado sesión (null = nadie)

    private ClienteSocket() { }
    
    public TrabajadorDTO enviarPeticion(PaqueteRed peticion)
            throws IOException, ClassNotFoundException, ErrorServidorException {
        usuarioActual = (TrabajadorDTO) pedir(peticion);
        return usuarioActual;
    }
    
    public synchronized Object pedir(PaqueteRed peticion)
            throws IOException, ClassNotFoundException, ErrorServidorException {
        try {
            conectar();
            out.writeObject(peticion);
            out.flush();
            PaqueteRed respuesta = (PaqueteRed) in.readObject();
            if (Acciones.ERROR.equals(respuesta.getAccion())) {
                throw new ErrorServidorException(String.valueOf(respuesta.getPayload()));
            }
            return respuesta.getPayload();
        } catch (IOException | ClassNotFoundException e) {
            cerrar();   // la conexión ya no es fiable: la próxima petición abrirá otra
            throw e;
        }
    }
    public synchronized void conectar() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;   // ya estamos conectados
        }
        Socket nuevo = new Socket();
        nuevo.connect(new InetSocketAddress(HOST, PUERTO), TIMEOUT_CONEXION_MS);
        nuevo.setSoTimeout(TIMEOUT_RESPUESTA_MS);
        socket = nuevo;
        out = new ObjectOutputStream(socket.getOutputStream());   // primero out...
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());      // ...y después in
    }
    public TrabajadorDTO getUsuarioActual() {
        return usuarioActual;
    }

    public synchronized void cerrar() {
        usuarioActual = null;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignorada) {
            // al cerrar no hay nada útil que hacer con el error
        }
        socket = null;
        out = null;
        in = null;
    }
}