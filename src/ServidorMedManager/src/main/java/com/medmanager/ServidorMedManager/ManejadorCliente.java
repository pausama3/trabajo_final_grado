package com.medmanager.ServidorMedManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.medmanager.ServidorMedManager.dao.CitaDAO;
import com.medmanager.ServidorMedManager.dao.HistorialMedicoDAO;
import com.medmanager.ServidorMedManager.dao.PacienteDAO;
import com.medmanager.ServidorMedManager.dao.TrabajadorDAO;
import com.medmanager.ServidorMedManager.modelo.Cita;
import com.medmanager.ServidorMedManager.modelo.Historial_Medico;
import com.medmanager.ServidorMedManager.modelo.Paciente;
import com.medmanager.ServidorMedManager.modelo.Trabajador;
import com.medmanager.ServidorMedManager.modelo.Trabajador.Rol;
import com.medmanager.comun.Acciones;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.HistorialDTO;
import com.medmanager.comun.InformeDTO;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

/**
 * Atiende a un cliente conectado durante toda la vida de su conexión.
 * <p>
 * Se ejecuta en un hilo propio. Lee peticiones {@link PaqueteRed} de forma
 * secuencial, comprueba la sesión y los permisos del usuario, delega la
 * operación en el DAO correspondiente y responde con {@link Acciones#OK} o
 * {@link Acciones#ERROR}. Los errores de una petición no cierran la conexión.
 */
public class ManejadorCliente implements Runnable {

    private final Socket socket;
    // Cada hilo tiene sus propios DAO (no guardan estado, solo abren sesiones de Hibernate)
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final HistorialMedicoDAO historialDAO = new HistorialMedicoDAO();
    private Trabajador sesion;   // null = nadie ha iniciado sesión en esta conexión

    /**
     * @param socket conexión ya aceptada con el cliente
     */
    public ManejadorCliente(Socket socket) {
        this.socket = socket;
    }

    /**
     * Bucle principal de la conexión: lee, valida, procesa y responde peticiones
     * hasta que el cliente se desconecta.
     */
    @Override
    public void run() {
        try (
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
        ) {
            out.flush();   // envía la cabecera para que el cliente pueda abrir su ObjectInputStream

            while (true) {   // una conexión, muchas peticiones
                Object recibido = in.readObject();   // bloquea hasta la siguiente petición

                // Se descarta cualquier objeto que no siga el protocolo
                if (!(recibido instanceof PaqueteRed)) {
                    responder(out, Acciones.ERROR, "Mensaje no válido");
                    continue;
                }
                PaqueteRed paquete = (PaqueteRed) recibido;
                String accion = paquete.getAccion();
                System.out.println("Acción recibida: " + accion);

                // ---- GUARDIÁN DE PERMISOS: se ejecuta antes de cualquier acción ----
                if (!Acciones.LOGIN.equals(accion)) {
                    if (sesion == null) {
                        responder(out, Acciones.ERROR, "Debes iniciar sesión");
                        continue;
                    }
                    if (!Permisos.puede(sesion.getRol(), accion)) {
                        System.out.println("⛔ " + sesion.getUsuario() + " (" + sesion.getRol()
                                + ") sin permiso para " + accion);
                        responder(out, Acciones.ERROR, "No tienes permiso para esta acción");
                        continue;
                    }
                }
                // --------------------------------------------------------------------

                // Despacho de la acción a su método correspondiente
                try {
                    switch (accion) {
                        case Acciones.LOGIN:
                            procesarLogin(paquete, out);
                            break;
                        case Acciones.BUSCAR_PACIENTES:
                            buscarPacientes(paquete, out);
                            break;
                        case Acciones.AGENDA_MEDICO:
                            agendaMedico(paquete, out);
                            break;
                        case Acciones.BUSCAR_INFORMES:
                            buscarInformes(paquete, out);
                            break;
                        case Acciones.GUARDAR_INFORME:
                            guardarInforme(paquete, out);
                            break;
                        case Acciones.AGENDA_CLINICA:
                            agendaClinica(paquete, out);
                            break;
                        case Acciones.LISTAR_MEDICOS:
                            listarMedicos(out);
                            break;
                        case Acciones.CREAR_CITA:
                            crearCita(paquete, out);
                            break;
                        case Acciones.CANCELAR_CITA:
                            cancelarCita(paquete, out);
                            break;
                        case Acciones.REPROGRAMAR_CITA:
                            reprogramarCita(paquete, out);
                            break;
                        case Acciones.GUARDAR_PACIENTE:
                            guardarPaciente(paquete, out);
                            break;
                        case Acciones.BAJA_PACIENTE:
                            bajaPaciente(paquete, out);
                            break;
                        case Acciones.CITAS_PACIENTE_HOY:
                            citasPacienteHoy(paquete, out);
                            break;
                        case Acciones.GUARDAR_OBSERVACIONES:
                            guardarObservaciones(paquete, out);
                            break;
                        default:
                            responder(out, Acciones.ERROR, "Acción desconocida: " + accion);
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    // Errores de negocio: el mensaje ya está pensado para el usuario
                    responder(out, Acciones.ERROR, e.getMessage());
                } catch (RuntimeException e) {
                    // Error de BD u otro inesperado: se avisa al cliente y la conexión sigue viva
                    System.err.println("Error procesando " + accion + ": " + e);
                    responder(out, Acciones.ERROR, ErroresBD.mensajeParaUsuario(e));
                }
            }

        } catch (EOFException e) {
            // Fin de flujo: el cliente ha cerrado la conexión de forma normal
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

    // ---------------------------------------------------------------- Sesión

    /**
     * Valida las credenciales y, si son correctas, asocia el trabajador a esta conexión.
     * Un login fallido anula cualquier sesión previa.
     */
    private void procesarLogin(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof LoginDTO)) {
            responder(out, Acciones.ERROR, "Datos de login no válidos");
            return;
        }
        LoginDTO login = (LoginDTO) paquete.getPayload();
        Trabajador trabajador = trabajadorDAO.autenticar(login.getUsuario(), login.getPassword());

        if (trabajador == null) {
            System.out.println("❌ Login fallido para: " + login.getUsuario());
            sesion = null;
            responder(out, Acciones.ERROR, "Usuario o contraseña incorrectos");
            return;
        }
        sesion = trabajador;   // el servidor recuerda quién es y qué rol tiene
        System.out.println("✅ Login correcto: " + trabajador.getUsuario() + " (" + trabajador.getRol() + ")");
        TrabajadorDTO dto = new TrabajadorDTO(
            trabajador.getId_trabajador(), trabajador.getNombre(),
            trabajador.getApellidos(), trabajador.getRol().name());
        responder(out, Acciones.OK, dto);
    }

    // ---------------------------------------------------------------- Pacientes

    /** Busca pacientes activos; las alergias solo se incluyen para el personal sanitario. */
    private void buscarPacientes(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        String texto = paquete.getPayload() instanceof String ? ((String) paquete.getPayload()).trim() : "";
        boolean conAlergias = sesion.getRol() != Rol.RECEPCIONISTA;   // RN3
        ArrayList<PacienteDTO> resultado = new ArrayList<>();
        for (Paciente p : pacienteDAO.buscar(texto)) {
            resultado.add(Conversor.aDTO(p, conAlergias));
        }
        responder(out, Acciones.OK, resultado);
    }

    // ---------------------------------------------------------------- Médico

    /** La agenda es siempre la del médico de la sesión: el cliente solo elige el día. */
    private void agendaMedico(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        LocalDate dia = paquete.getPayload() instanceof LocalDate ? (LocalDate) paquete.getPayload() : LocalDate.now();
        ArrayList<CitaDTO> resultado = new ArrayList<>();
        for (Cita c : citaDAO.agendaDeMedico(sesion.getId_trabajador(), dia)) {
            resultado.add(Conversor.aDTO(c, true));
        }
        responder(out, Acciones.OK, resultado);
    }

    /** Historial completo (de cualquier médico) de los pacientes que coinciden con el texto. */
    private void buscarInformes(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        String texto = paquete.getPayload() instanceof String ? ((String) paquete.getPayload()).trim() : "";
        ArrayList<HistorialDTO> resultado = new ArrayList<>();
        if (!texto.isEmpty()) {   // sin texto no se devuelve nada: no se lista el historial de toda la clínica
            for (Historial_Medico h : historialDAO.buscarPorPaciente(texto)) {
                resultado.add(Conversor.aDTO(h));
            }
        }
        responder(out, Acciones.OK, resultado);
    }

    /**
     * Registra el informe de una consulta. El médico se toma de la sesión, no del
     * cliente, para que nadie pueda cerrar citas de otro médico.
     */
    private void guardarInforme(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof InformeDTO)) {
            responder(out, Acciones.ERROR, "Datos del informe no válidos");
            return;
        }
        InformeDTO inf = (InformeDTO) paquete.getPayload();
        String diagnostico = vacioANull(inf.getDiagnostico());
        if (diagnostico == null) {
            responder(out, Acciones.ERROR, "El diagnóstico es obligatorio");
            return;
        }
        Historial_Medico guardado = historialDAO.registrarConsulta(
                inf.getIdCita(), sesion.getId_trabajador(), diagnostico,
                vacioANull(inf.getTratamiento()), vacioANull(inf.getAlergias()));
        System.out.println("📝 Informe guardado para la cita " + inf.getIdCita() + " por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(guardado));
    }

    // ---------------------------------------------------------------- Recepción

    /** Agenda de todos los médicos para un día (hoy si no se indica). */
    private void agendaClinica(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        LocalDate dia = paquete.getPayload() instanceof LocalDate ? (LocalDate) paquete.getPayload() : LocalDate.now();
        ArrayList<CitaDTO> resultado = new ArrayList<>();
        for (Cita c : citaDAO.agendaDelDia(dia)) {
            resultado.add(Conversor.aDTO(c, false));   // RN3: recepción no ve alergias
        }
        responder(out, Acciones.OK, resultado);
    }

    /** Médicos con su especialidad, para elegir a quién se asigna una cita. */
    private void listarMedicos(ObjectOutputStream out) throws IOException {
        ArrayList<TrabajadorDTO> resultado = new ArrayList<>();
        for (Trabajador t : trabajadorDAO.listarMedicos()) {
            resultado.add(Conversor.aDTO(t));
        }
        responder(out, Acciones.OK, resultado);
    }

    /** Valida los datos de la nueva cita y la registra como PENDIENTE. */
    private void crearCita(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof CitaDTO)) {
            responder(out, Acciones.ERROR, "Datos de la cita no válidos");
            return;
        }
        CitaDTO dto = (CitaDTO) paquete.getPayload();
        LocalDateTime fh = dto.getFechaHora();
        comprobarFechaHora(fh);
        String motivo = vacioANull(dto.getMotivoConsulta());
        if (motivo != null && motivo.length() > 255) {
            throw new IllegalArgumentException("El motivo no puede superar 255 caracteres");
        }
        Cita cita = citaDAO.crear(dto.getIdPaciente(), dto.getIdTrabajador(), fh, motivo);
        System.out.println("📅 Cita " + cita.getId_cita() + " creada por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, false));
    }

    /** Cancela una cita pendiente a partir de su id. */
    private void cancelarCita(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof Integer)) {
            responder(out, Acciones.ERROR, "Cita no válida");
            return;
        }
        Cita cita = citaDAO.cancelar((Integer) paquete.getPayload());
        System.out.println("🚫 Cita " + cita.getId_cita() + " cancelada por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, false));
    }

    /** Mueve una cita PENDIENTE a otra fecha/hora con el mismo médico (RF2, RN5). */
    private void reprogramarCita(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof CitaDTO)) {
            responder(out, Acciones.ERROR, "Datos de la cita no válidos");
            return;
        }
        CitaDTO dto = (CitaDTO) paquete.getPayload();
        comprobarFechaHora(dto.getFechaHora());
        Cita cita = citaDAO.reprogramar(dto.getId(), dto.getFechaHora());
        System.out.println("🔁 Cita " + cita.getId_cita() + " reprogramada a " + cita.getFecha_hora()
                + " por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, false));
    }

    /** Reglas de la fecha/hora de una cita: franja de 15 minutos (RN1) y nunca en el pasado. */
    private static void comprobarFechaHora(LocalDateTime fh) {
        if (fh == null) {
            throw new IllegalArgumentException("Falta la fecha y hora de la cita");
        }
        if (fh.getMinute() % 15 != 0 || fh.getSecond() != 0 || fh.getNano() != 0) {
            throw new IllegalArgumentException("Las citas van en franjas de 15 minutos (:00, :15, :30, :45)");
        }
        if (fh.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede dar una cita en una fecha u hora ya pasada");
        }
    }

    /** Alta (id 0) o modificación de los datos personales. Las alergias nunca se tocan desde aquí (RN3). */
    private void guardarPaciente(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof PacienteDTO)) {
            responder(out, Acciones.ERROR, "Datos del paciente no válidos");
            return;
        }
        PacienteDTO dto = (PacienteDTO) paquete.getPayload();
        String dni = vacioANull(dto.getDni());
        String nombre = vacioANull(dto.getNombre());
        String apellidos = vacioANull(dto.getApellidos());
        String telefono = vacioANull(dto.getTelefono());
        String email = vacioANull(dto.getEmail());
        String direccion = vacioANull(dto.getDireccion());

        // ---- Validaciones: el servidor no se fía de lo que haya comprobado el cliente ----
        // DNI: 8 dígitos + letra; NIE: X/Y/Z + 7 dígitos + letra
        if (dni == null || !dni.toUpperCase().matches("\\d{8}[A-Z]|[XYZ]\\d{7}[A-Z]")) {
            throw new IllegalArgumentException("El DNI/NIE no es válido (ej.: 12345678A o X1234567B)");
        }
        dni = dni.toUpperCase();
        comprobarTexto(nombre, "nombre", 50, true);
        comprobarTexto(apellidos, "apellidos", 100, true);
        comprobarTexto(telefono, "teléfono", 15, false);
        comprobarTexto(email, "email", 100, false);
        comprobarTexto(direccion, "dirección", 255, false);
        // Formato básico de email: algo@dominio.ext
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        if (dto.getFechaNacimiento() != null && dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        Paciente guardado;
        if (dto.getId() == 0) {   // alta: el paciente se crea activo y sin alergias registradas
            guardado = new Paciente(dni, dto.getFechaNacimiento(), nombre, apellidos, telefono,
                    email, direccion, null, true);
            pacienteDAO.save(guardado);
            System.out.println("➕ Paciente " + dni + " dado de alta por " + sesion.getUsuario());
        } else {   // modificación de un paciente existente
            guardado = pacienteDAO.modificarDatos(dto.getId(), dni, nombre, apellidos, telefono,
                    email, direccion, dto.getFechaNacimiento());
            System.out.println("✏️ Paciente " + dni + " modificado por " + sesion.getUsuario());
        }
        responder(out, Acciones.OK, Conversor.aDTO(guardado, false));
    }

    /** Baja lógica de un paciente; responde con un resumen que incluye las citas canceladas. */
    private void bajaPaciente(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof Integer)) {
            responder(out, Acciones.ERROR, "Paciente no válido");
            return;
        }
        int canceladas = pacienteDAO.darDeBaja((Integer) paquete.getPayload());
        System.out.println("➖ Paciente " + paquete.getPayload() + " dado de baja por " + sesion.getUsuario());
        responder(out, Acciones.OK, canceladas == 0
                ? "Paciente dado de baja."
                : "Paciente dado de baja. Se han cancelado " + canceladas + " cita(s) pendiente(s).");
    }

    // ---------------------------------------------------------------- Enfermería

    /** Citas de hoy del paciente para la ficha rápida, con datos clínicos (RN3: enfermería sí los ve). */
    private void citasPacienteHoy(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof Integer)) {
            responder(out, Acciones.ERROR, "Paciente no válido");
            return;
        }
        ArrayList<CitaDTO> resultado = new ArrayList<>();
        for (Cita c : citaDAO.citasDePacienteEnDia((Integer) paquete.getPayload(), LocalDate.now())) {
            resultado.add(Conversor.aDTO(c, true));
        }
        responder(out, Acciones.OK, resultado);
    }

    /**
     * Anota las observaciones de enfermería en una cita PENDIENTE de hoy (RN4).
     * Un texto vacío borra las observaciones anteriores.
     */
    private void guardarObservaciones(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof CitaDTO)) {
            responder(out, Acciones.ERROR, "Datos de la cita no válidos");
            return;
        }
        CitaDTO dto = (CitaDTO) paquete.getPayload();
        String observaciones = vacioANull(dto.getObservacionesEnfermeria());
        comprobarTexto(observaciones, "observaciones", MAX_OBSERVACIONES, false);
        Cita cita = citaDAO.guardarObservaciones(dto.getId(), observaciones);
        System.out.println("🩺 Observaciones guardadas en la cita " + cita.getId_cita() + " por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, true));
    }

    // ---------------------------------------------------------------- Utilidades

    /** Longitud máxima de las observaciones de enfermería (la columna es TEXT; se limita para evitar abusos). */
    private static final int MAX_OBSERVACIONES = 2000;

    /**
     * Comprueba la obligatoriedad y la longitud máxima de un campo de texto.
     *
     * @throws IllegalArgumentException con un mensaje para el usuario si no es válido
     */
    private static void comprobarTexto(String valor, String campo, int max, boolean obligatorio) {
        if (valor == null) {
            if (obligatorio) throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
            return;
        }
        if (valor.length() > max) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + max + " caracteres");
        }
    }

    /** Recorta espacios y convierte las cadenas vacías en {@code null} (así se guardan en la BD). */
    private static String vacioANull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    /** Envía una respuesta al cliente y vacía el búfer para que llegue de inmediato. */
    private void responder(ObjectOutputStream out, String accion, Object payload) throws IOException {
        out.writeObject(new PaqueteRed(accion, payload));
        out.flush();
    }
}