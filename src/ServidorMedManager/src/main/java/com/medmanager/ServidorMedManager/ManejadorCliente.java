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

public class ManejadorCliente implements Runnable {

    private final Socket socket;
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final HistorialMedicoDAO historialDAO = new HistorialMedicoDAO();
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
                        case Acciones.GUARDAR_PACIENTE:
                            guardarPaciente(paquete, out);
                            break;
                        case Acciones.BAJA_PACIENTE:
                            bajaPaciente(paquete, out);
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

    private void agendaClinica(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        LocalDate dia = paquete.getPayload() instanceof LocalDate ? (LocalDate) paquete.getPayload() : LocalDate.now();
        ArrayList<CitaDTO> resultado = new ArrayList<>();
        for (Cita c : citaDAO.agendaDelDia(dia)) {
            resultado.add(Conversor.aDTO(c, false));   // RN3: recepción no ve alergias
        }
        responder(out, Acciones.OK, resultado);
    }

    private void listarMedicos(ObjectOutputStream out) throws IOException {
        ArrayList<TrabajadorDTO> resultado = new ArrayList<>();
        for (Trabajador t : trabajadorDAO.listarMedicos()) {
            resultado.add(Conversor.aDTO(t));
        }
        responder(out, Acciones.OK, resultado);
    }

    private void crearCita(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof CitaDTO)) {
            responder(out, Acciones.ERROR, "Datos de la cita no válidos");
            return;
        }
        CitaDTO dto = (CitaDTO) paquete.getPayload();
        LocalDateTime fh = dto.getFechaHora();
        if (fh == null) {
            throw new IllegalArgumentException("Falta la fecha y hora de la cita");
        }
        if (fh.getMinute() % 15 != 0 || fh.getSecond() != 0 || fh.getNano() != 0) {
            throw new IllegalArgumentException("Las citas van en franjas de 15 minutos (:00, :15, :30, :45)");
        }
        if (fh.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se puede dar una cita en una fecha u hora ya pasada");
        }
        String motivo = vacioANull(dto.getMotivoConsulta());
        if (motivo != null && motivo.length() > 255) {
            throw new IllegalArgumentException("El motivo no puede superar 255 caracteres");
        }
        Cita cita = citaDAO.crear(dto.getIdPaciente(), dto.getIdTrabajador(), fh, motivo);
        System.out.println("📅 Cita " + cita.getId_cita() + " creada por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, false));
    }

    private void cancelarCita(PaqueteRed paquete, ObjectOutputStream out) throws IOException {
        if (!(paquete.getPayload() instanceof Integer)) {
            responder(out, Acciones.ERROR, "Cita no válida");
            return;
        }
        Cita cita = citaDAO.cancelar((Integer) paquete.getPayload());
        System.out.println("🚫 Cita " + cita.getId_cita() + " cancelada por " + sesion.getUsuario());
        responder(out, Acciones.OK, Conversor.aDTO(cita, false));
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

        if (dni == null || !dni.toUpperCase().matches("\\d{8}[A-Z]|[XYZ]\\d{7}[A-Z]")) {
            throw new IllegalArgumentException("El DNI/NIE no es válido (ej.: 12345678A o X1234567B)");
        }
        dni = dni.toUpperCase();
        comprobarTexto(nombre, "nombre", 50, true);
        comprobarTexto(apellidos, "apellidos", 100, true);
        comprobarTexto(telefono, "teléfono", 15, false);
        comprobarTexto(email, "email", 100, false);
        comprobarTexto(direccion, "dirección", 255, false);
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new IllegalArgumentException("El email no es válido");
        }
        if (dto.getFechaNacimiento() != null && dto.getFechaNacimiento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        }

        Paciente guardado;
        if (dto.getId() == 0) {
            guardado = new Paciente(dni, dto.getFechaNacimiento(), nombre, apellidos, telefono,
                    email, direccion, null, true);
            pacienteDAO.save(guardado);
            System.out.println("➕ Paciente " + dni + " dado de alta por " + sesion.getUsuario());
        } else {
            guardado = pacienteDAO.modificarDatos(dto.getId(), dni, nombre, apellidos, telefono,
                    email, direccion, dto.getFechaNacimiento());
            System.out.println("✏️ Paciente " + dni + " modificado por " + sesion.getUsuario());
        }
        responder(out, Acciones.OK, Conversor.aDTO(guardado, false));
    }

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

    private static void comprobarTexto(String valor, String campo, int max, boolean obligatorio) {
        if (valor == null) {
            if (obligatorio) throw new IllegalArgumentException("El campo " + campo + " es obligatorio");
            return;
        }
        if (valor.length() > max) {
            throw new IllegalArgumentException("El campo " + campo + " no puede superar " + max + " caracteres");
        }
    }

    private static String vacioANull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private void responder(ObjectOutputStream out, String accion, Object payload) throws IOException {
        out.writeObject(new PaqueteRed(accion, payload));
        out.flush();
    }
}