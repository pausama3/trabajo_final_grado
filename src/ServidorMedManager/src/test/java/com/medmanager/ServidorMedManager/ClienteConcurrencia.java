package com.medmanager.ServidorMedManager;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

/**
 * Pruebas de concurrencia (apartado 8.2.5 de la memoria).
 * <p>
 * Lanza varios clientes en hilos distintos que envían sus peticiones a la vez
 * (una barrera los suelta todos en el mismo instante) para comprobar que el
 * servidor multihilo los atiende en paralelo y que las reglas RN1 y RN2 se
 * cumplen aunque dos recepcionistas reserven la misma franja al mismo tiempo.
 * <p>
 * Necesita el servidor arrancado: Run As > Java Application. Las citas que crea
 * se cancelan al final, así que la franja vuelve a quedar libre.
 */
public class ClienteConcurrencia {

    private static final String HOST = "localhost";
    private static final int PUERTO = 8080;
    private static final int CLIENTES_PC01 = 20;

    private static int correctas = 0;
    private static int total = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== Pruebas de concurrencia ===\n");
        ExecutorService hilos = Executors.newFixedThreadPool(CLIENTES_PC01);

        // Datos de partida: ids de los pacientes y de los médicos del script 03
        int idAna, idPedro, idMedico1, idMedico2;
        try (Conexion c = new Conexion()) {
            c.login("recepcion1", "Recep123!");
            idAna = primerPaciente(c, "55555555E");
            idPedro = primerPaciente(c, "66666666F");
            @SuppressWarnings("unchecked")
            List<TrabajadorDTO> medicos = (List<TrabajadorDTO>) c.enviar(Acciones.LISTAR_MEDICOS, null).getPayload();
            idMedico1 = medicos.get(0).getId();
            idMedico2 = medicos.get(1).getId();
        }
        // Franjas de dentro de 30 días, para no chocar con citas reales
        LocalDateTime franja1 = LocalDateTime.of(LocalDate.now().plusDays(30), LocalTime.of(10, 0));
        LocalDateTime franja2 = franja1.plusMinutes(15);
        List<Integer> citasCreadas = new ArrayList<>();

        // PC-01: muchos clientes a la vez (login + búsqueda cada uno)
        {
            CyclicBarrier salida = new CyclicBarrier(CLIENTES_PC01);
            List<Future<Boolean>> resultados = new ArrayList<>();
            long inicio = System.currentTimeMillis();
            for (int i = 0; i < CLIENTES_PC01; i++) {
                resultados.add(hilos.submit(() -> {
                    try (Conexion c = new Conexion()) {
                        salida.await();   // todos empiezan a la vez
                        c.login("recepcion1", "Recep123!");
                        return Acciones.OK.equals(c.enviar(Acciones.BUSCAR_PACIENTES, "Pérez").getAccion());
                    }
                }));
            }
            int ok = 0;
            for (Future<Boolean> f : resultados) if (f.get()) ok++;
            long ms = System.currentTimeMillis() - inicio;
            anotar("PC-01", CLIENTES_PC01 + " clientes a la vez -> " + ok + " respuestas OK en " + ms + " ms",
                    ok == CLIENTES_PC01);
        }

        // PC-02: un cliente conectado que no envía nada no bloquea a los demás
        try (Conexion parado = new Conexion()) {   // se queda abierto sin hablar
            long inicio = System.currentTimeMillis();
            boolean ok;
            try (Conexion c = new Conexion()) {
                c.login("medico1", "Medico123!");
                ok = Acciones.OK.equals(c.enviar(Acciones.BUSCAR_PACIENTES, "Sanz").getAccion());
            }
            long ms = System.currentTimeMillis() - inicio;
            anotar("PC-02", "con un cliente conectado y parado, otro cliente es atendido en " + ms + " ms", ok);
        }

        // PC-03: dos recepcionistas reservan al mismo médico en la misma franja (RN1)
        {
            List<PaqueteRed> r = aLaVez(hilos,
                    crearCita(CitaDTO.nueva(idAna, idMedico1, franja1, "Prueba concurrencia")),
                    crearCita(CitaDTO.nueva(idPedro, idMedico1, franja1, "Prueba concurrencia")));
            guardarCreadas(r, citasCreadas);
            anotar("PC-03", "mismo médico y franja -> " + describir(r),
                    unaOkUnaError(r, "El médico ya tiene una cita a esa hora"));
        }

        // PC-04: dos recepcionistas dan cita al mismo paciente en la misma franja con médicos distintos (RN2)
        {
            List<PaqueteRed> r = aLaVez(hilos,
                    crearCita(CitaDTO.nueva(idAna, idMedico1, franja2, "Prueba concurrencia")),
                    crearCita(CitaDTO.nueva(idAna, idMedico2, franja2, "Prueba concurrencia")));
            guardarCreadas(r, citasCreadas);
            anotar("PC-04", "mismo paciente y franja -> " + describir(r),
                    unaOkUnaError(r, "El paciente ya tiene otra cita a esa hora"));
        }

        // Limpieza: se cancelan las citas creadas por las pruebas
        try (Conexion c = new Conexion()) {
            c.login("recepcion1", "Recep123!");
            for (int id : citasCreadas) c.enviar(Acciones.CANCELAR_CITA, id);
        }
        System.out.println("\n(Se han cancelado las " + citasCreadas.size() + " citas creadas por las pruebas)");
        System.out.println("Resultado: " + correctas + " de " + total + " pruebas correctas");
        hilos.shutdown();
    }

    // ------------------------------------------------------------ Ayudas

    /** Tarea de un recepcionista que crea una cita en su propia conexión. */
    private static Callable<PaqueteRed> crearCita(CitaDTO cita) {
        return () -> {
            try (Conexion c = new Conexion()) {
                c.login("recepcion1", "Recep123!");
                Barrera.esperar();
                return c.enviar(Acciones.CREAR_CITA, cita);
            }
        };
    }

    /** Ejecuta dos tareas en hilos distintos y las suelta en el mismo instante. */
    private static List<PaqueteRed> aLaVez(ExecutorService hilos, Callable<PaqueteRed> a, Callable<PaqueteRed> b)
            throws Exception {
        Barrera.preparar(2);
        Future<PaqueteRed> fa = hilos.submit(a);
        Future<PaqueteRed> fb = hilos.submit(b);
        List<PaqueteRed> r = new ArrayList<>();
        r.add(fa.get());
        r.add(fb.get());
        return r;
    }

    /** Barrera compartida por las dos tareas de una prueba. */
    private static class Barrera {
        private static CyclicBarrier barrera;
        static void preparar(int partes) { barrera = new CyclicBarrier(partes); }
        static void esperar() throws Exception { barrera.await(); }
    }

    private static boolean unaOkUnaError(List<PaqueteRed> r, String error) {
        int ok = 0, err = 0;
        for (PaqueteRed p : r) {
            if (Acciones.OK.equals(p.getAccion())) ok++;
            else if (Acciones.ERROR.equals(p.getAccion()) && error.equals(p.getPayload())) err++;
        }
        return ok == 1 && err == 1;
    }

    private static String describir(List<PaqueteRed> r) {
        StringBuilder sb = new StringBuilder();
        for (PaqueteRed p : r) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(Acciones.OK.equals(p.getAccion()) ? "OK (cita creada)" : "ERROR: " + p.getPayload());
        }
        return sb.toString();
    }

    private static void guardarCreadas(List<PaqueteRed> r, List<Integer> creadas) {
        for (PaqueteRed p : r) {
            if (Acciones.OK.equals(p.getAccion()) && p.getPayload() instanceof CitaDTO) {
                creadas.add(((CitaDTO) p.getPayload()).getId());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static int primerPaciente(Conexion c, String dni) throws Exception {
        List<PacienteDTO> lista = (List<PacienteDTO>) c.enviar(Acciones.BUSCAR_PACIENTES, dni).getPayload();
        return lista.get(0).getId();
    }

    private static void anotar(String id, String texto, boolean ok) {
        total++;
        if (ok) correctas++;
        System.out.println(id + " | " + texto + "  [" + (ok ? "OK" : "FALLO") + "]");
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
