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

/**
 * Singleton: una única conexión con el servidor para toda la aplicación.
 * <p>
 * Encapsula el socket TCP y los flujos de objetos serializados. Todas las
 * peticiones pasan por {@link #pedir(PaqueteRed)}, que está sincronizado para
 * que nunca se mezclen dos peticiones/respuestas sobre el mismo flujo.
 * La conexión se abre de forma perezosa y se reabre automáticamente si se pierde.
 */
public class ClienteSocket {

    /** Instancia única, creada al cargar la clase. */
    private static final ClienteSocket INSTANCIA = new ClienteSocket();

    /**
     * @return la instancia única del cliente
     */
    public static ClienteSocket getInstance() {
        return INSTANCIA;
    }

    // ---- Configuración de la conexión ----
    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;
    private static final int TIMEOUT_CONEXION_MS = 3000;    // espera máxima para establecer la conexión
    private static final int TIMEOUT_RESPUESTA_MS = 10000;  // espera máxima para recibir una respuesta

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private TrabajadorDTO usuarioActual;   // quién ha iniciado sesión (null = nadie)

    /** Constructor privado: solo se accede mediante {@link #getInstance()}. */
    private ClienteSocket() { }

    /**
     * Envía la petición de inicio de sesión y guarda el trabajador autenticado
     * como usuario actual.
     *
     * @param peticion paquete {@link Acciones#LOGIN} con un {@link LoginDTO}
     * @return el trabajador que ha iniciado sesión
     * @throws IOException             si falla la comunicación con el servidor
     * @throws ClassNotFoundException  si la respuesta no se puede deserializar
     * @throws ErrorServidorException  si las credenciales no son válidas
     */
    public TrabajadorDTO enviarPeticion(PaqueteRed peticion)
            throws IOException, ClassNotFoundException, ErrorServidorException {
        usuarioActual = (TrabajadorDTO) pedir(peticion);
        return usuarioActual;
    }

    /**
     * Envía una petición al servidor y espera su respuesta.
     *
     * @param peticion paquete con la acción y sus datos
     * @return el payload de la respuesta {@link Acciones#OK}
     * @throws IOException             si falla la comunicación (la conexión se cierra)
     * @throws ClassNotFoundException  si la respuesta no se puede deserializar
     * @throws ErrorServidorException  si el servidor responde con {@link Acciones#ERROR}
     */
    public synchronized Object pedir(PaqueteRed peticion)
            throws IOException, ClassNotFoundException, ErrorServidorException {
        try {
            conectar();
            out.writeObject(peticion);
            out.flush();
            PaqueteRed respuesta = (PaqueteRed) in.readObject();
            // Un ERROR del servidor es un fallo de negocio: la conexión sigue siendo válida
            if (Acciones.ERROR.equals(respuesta.getAccion())) {
                throw new ErrorServidorException(String.valueOf(respuesta.getPayload()));
            }
            return respuesta.getPayload();
        } catch (IOException | ClassNotFoundException e) {
            cerrar();   // la conexión ya no es fiable: la próxima petición abrirá otra
            throw e;
        }
    }

    /**
     * Abre la conexión con el servidor si no está ya abierta.
     *
     * @throws IOException si no se puede conectar en el tiempo establecido
     */
    public synchronized void conectar() throws IOException {
        if (socket != null && !socket.isClosed()) {
            return;   // ya estamos conectados
        }
        Socket nuevo = new Socket();
        nuevo.connect(new InetSocketAddress(HOST, PUERTO), TIMEOUT_CONEXION_MS);
        nuevo.setSoTimeout(TIMEOUT_RESPUESTA_MS);
        socket = nuevo;
        // El orden importa: el ObjectInputStream se bloquea hasta leer la cabecera
        // del otro extremo, así que hay que crear y vaciar antes el de salida
        out = new ObjectOutputStream(socket.getOutputStream());   // primero out...
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());      // ...y después in
    }

    /**
     * @return el trabajador con sesión iniciada, o {@code null} si no hay ninguno
     */
    public TrabajadorDTO getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Cierra la conexión y olvida al usuario actual (cierre de sesión).
     * Es seguro llamarlo aunque la conexión ya esté cerrada.
     */
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