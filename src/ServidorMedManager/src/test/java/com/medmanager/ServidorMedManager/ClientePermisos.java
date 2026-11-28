package com.medmanager.ServidorMedManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.InformeDTO;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.PaqueteRed;

/**
 * Pruebas de permisos y seguridad (apartado 8.2.4 de la memoria).
 * <p>
 * Hace de cliente "malicioso": se salta las aplicaciones y envía los paquetes
 * directamente al puerto del servidor, para comprobar que es el servidor el que
 * aplica los permisos y no la interfaz.
 * <p>
 * No es una prueba JUnit: necesita el servidor arrancado. Se ejecuta con
 * Run As > Java Application. Ninguna petición modifica datos: las que escriben
 * (GUARDAR_INFORME, CREAR_CITA) deben ser rechazadas antes de llegar a la base de datos.
 */
public class ClientePermisos {

    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;

    private static final String NO_PERMISO = "No tienes permiso para esta acción";

    private static int correctas = 0;
    private static int total = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pruebas de permisos y seguridad ===\n");

        // PS-01: petición sin haber iniciado sesión
        try (Conexion c = new Conexion()) {
            PaqueteRed r = c.enviar(Acciones.BUSCAR_PACIENTES, "Pérez");
            comprobar("PS-01", "BUSCAR_PACIENTES sin login", r, "Debes iniciar sesión");
        }

        // PS-02: un login fallido no deja sesión abierta en la conexión
        try (Conexion c = new Conexion()) {
            PaqueteRed r1 = c.enviar(Acciones.LOGIN, new LoginDTO("recepcion1", "Incorrecta1"));
            PaqueteRed r2 = c.enviar(Acciones.BUSCAR_PACIENTES, "Pérez");
            comprobar("PS-02", "login fallido", r1, "Usuario o contraseña incorrectos");
            comprobar("PS-02", "petición después del login fallido", r2, "Debes iniciar sesión");
        }

        // PS-03: recepción intenta leer el historial clínico (RN3)
        try (Conexion c = new Conexion()) {
            c.login("recepcion1", "Recep123!");
            PaqueteRed r = c.enviar(Acciones.BUSCAR_INFORMES, "Pérez");
            comprobar("PS-03", "recepción pide BUSCAR_INFORMES", r, NO_PERMISO);
        }

        // PS-04: recepción busca pacientes; el servidor no le envía las alergias (RN3)
        try (Conexion c = new Conexion()) {
            c.login("recepcion1", "Recep123!");
            PaqueteRed r = c.enviar(Acciones.BUSCAR_PACIENTES, "Pérez");
            comprobarSinAlergias("PS-04", r);
        }

        // PS-05: enfermería intenta escribir un informe médico (RN4)
        try (Conexion c = new Conexion()) {
            c.login("enfermero1", "Enfer123!");
            PaqueteRed r = c.enviar(Acciones.GUARDAR_INFORME,
                    new InformeDTO(-1, "Diagnóstico falso", "Tratamiento falso", null));
            comprobar("PS-05", "enfermería pide GUARDAR_INFORME", r, NO_PERMISO);
        }

        // PS-06: el médico intenta crear una cita, que es tarea de recepción (RN5)
        try (Conexion c = new Conexion()) {
            c.login("medico1", "Medico123!");
            PaqueteRed r = c.enviar(Acciones.CREAR_CITA,
                    CitaDTO.nueva(-1, -1, LocalDateTime.now().plusDays(1), "Prueba"));
            comprobar("PS-06", "médico pide CREAR_CITA", r, NO_PERMISO);
        }

        // PS-07: acción que no existe en el protocolo (se deniega por defecto)
        try (Conexion c = new Conexion()) {
            c.login("medico1", "Medico123!");
            PaqueteRed r = c.enviar("BORRAR_TODO", null);
            comprobar("PS-07", "acción inventada BORRAR_TODO", r, NO_PERMISO);
        }

        System.out.println("\nResultado: " + correctas + " de " + total + " comprobaciones correctas");
    }

    // ------------------------------------------------------------ Comprobaciones

    /** La respuesta debe ser ERROR con el mensaje esperado. */
    private static void comprobar(String id, String prueba, PaqueteRed r, String esperado) {
        total++;
        boolean ok = Acciones.ERROR.equals(r.getAccion()) && esperado.equals(r.getPayload());
        if (ok) correctas++;
        System.out.println(id + " | " + prueba + " -> " + r.getAccion() + ": " + r.getPayload()
                + "  [" + (ok ? "OK" : "FALLO") + "]");
    }

    /** La respuesta debe ser OK con pacientes y ninguno debe llevar alergias. */
    @SuppressWarnings("unchecked")
    private static void comprobarSinAlergias(String id, PaqueteRed r) {
        total++;
        boolean ok = false;
        int pacientes = 0;
        if (Acciones.OK.equals(r.getAccion()) && r.getPayload() instanceof List) {
            List<PacienteDTO> lista = (List<PacienteDTO>) r.getPayload();
            pacientes = lista.size();
            ok = pacientes > 0;
            for (PacienteDTO p : lista) {
                System.out.println("     " + p.getNombre() + " " + p.getApellidos()
                        + " -> alergias = " + p.getAlergias());
                if (p.getAlergias() != null) ok = false;
            }
        }
        if (ok) correctas++;
        System.out.println(id + " | recepción busca pacientes -> " + r.getAccion() + ", "
                + pacientes + " paciente(s) sin alergias  [" + (ok ? "OK" : "FALLO") + "]");
    }

    // ------------------------------------------------------------ Conexión

    /** Una conexión con el servidor, igual que la que abren las aplicaciones. */
    private static class Conexion implements AutoCloseable {
        private final Socket socket;
        private final ObjectOutputStream out;
        private final ObjectInputStream in;

        Conexion() throws Exception {
            socket = new Socket(HOST, PUERTO);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        }

        PaqueteRed enviar(String accion, Object payload) throws Exception {
            out.writeObject(new PaqueteRed(accion, payload));
            out.flush();
            out.reset();
            return (PaqueteRed) in.readObject();
        }

        void login(String usuario, String password) throws Exception {
            PaqueteRed r = enviar(Acciones.LOGIN, new LoginDTO(usuario, password));
            if (!Acciones.OK.equals(r.getAccion())) {
                throw new IllegalStateException("No se pudo iniciar sesión con " + usuario + ": " + r.getPayload());
            }
        }

        @Override
        public void close() throws Exception {
            socket.close();
        }
    }
}
