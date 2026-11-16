package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

/** Pantalla principal de recepción: agenda de la clínica, gestión de pacientes y alta de citas. */
public class DashboardRecepcion extends DashboardBase {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Horario de la clínica para ofrecer huecos (franjas de 15 minutos, RN1)
    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(20, 0);

    // ---- Agenda ----
    private final PanelBase agenda;
    private final SelectorDia selectorDia;
    private final JComboBox<TrabajadorDTO> cmbFiltroMedico = new JComboBox<>();
    private List<CitaDTO> citasDelDia = new ArrayList<>();
    private final List<CitaDTO> citasVisibles = new ArrayList<>();   // mismas posiciones que las filas

    // ---- Pacientes ----
    private final PanelBase pacientes;
    private final List<PacienteDTO> pacientesVisibles = new ArrayList<>();

    // ---- Registrar cita ----
    private final JTextField txtBuscarPacienteCita = campoTexto();
    private final JComboBox<PacienteDTO> cmbPaciente = new JComboBox<>();
    private final JComboBox<TrabajadorDTO> cmbMedico = new JComboBox<>();
    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<LocalTime> cmbHora = new JComboBox<>();
    private final JTextField txtMotivo = campoTexto();
    private final JLabel lblHuecos = new JLabel(" ");
    private final JButton btnRegistrarCita = new JButton("Registrar cita");

    public DashboardRecepcion(TrabajadorDTO usuario) {
        super(usuario, "Recepción");

        // 1. Agenda de la clínica
        agenda = new PanelBase("Agenda de la clínica", "Todas las citas, por día y por médico",
                new String[] {"Hora", "Paciente", "Médico", "Motivo", "Estado"}, false);
        selectorDia = new SelectorDia(this::cargarAgenda);
        agenda.add(crearBarraAgenda(), BorderLayout.SOUTH);
        anadirPantalla("Agenda", agenda);

        // 2. Pacientes
        pacientes = new PanelBase("Pacientes", "Busca, da de alta, modifica o da de baja",
                new String[] {"DNI", "Nombre", "Teléfono", "Email", "Fecha de nacimiento"}, true);
        pacientes.getBtnBuscar().addActionListener(e -> buscarPacientes());
        pacientes.getTxtBusqueda().addActionListener(e -> buscarPacientes());
        pacientes.add(crearBarraPacientes(), BorderLayout.SOUTH);
        pacientes.getTabla().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) modificarPaciente();   // doble clic = modificar
            }
        });
        anadirPantalla("Pacientes", pacientes);

        // 3. Registrar cita
        anadirPantalla("Registrar cita", crearPantallaCita());

        // Carga inicial
        cargarMedicos();
        cargarAgenda();
        buscarPacientes();
    }

    // =================================================================== AGENDA

    private JPanel crearBarraAgenda() {
        cmbFiltroMedico.setFont(Estilo.FUENTE);
        textoCombo(cmbFiltroMedico, m -> m.getNombre() + " " + m.getApellidos(), "Todos los médicos");
        cmbFiltroMedico.addActionListener(e -> mostrarAgendaFiltrada());

        JButton btnCancelar = new JButton("Cancelar cita seleccionada");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> cancelarCitaSeleccionada());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filtros.setOpaque(false);
        filtros.add(etiqueta("Médico:"));
        filtros.add(cmbFiltroMedico);
        filtros.add(btnCancelar);

        JPanel barra = new JPanel();
        barra.setOpaque(false);
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.add(selectorDia);
        barra.add(javax.swing.Box.createVerticalStrut(10));
        barra.add(filtros);
        return barra;
    }

    private void cargarAgenda() {
        LocalDate dia = selectorDia.getDia();
        this.<List<CitaDTO>>pedirAlServidor(new PaqueteRed(Acciones.AGENDA_CLINICA, dia), lista -> {
            citasDelDia = lista;
            mostrarAgendaFiltrada();
        });
    }

    /** Pinta las citas del día, solo las del médico elegido si hay filtro (se filtra aquí, sin ir al servidor). */
    private void mostrarAgendaFiltrada() {
        TrabajadorDTO filtro = (TrabajadorDTO) cmbFiltroMedico.getSelectedItem();
        citasVisibles.clear();
        agenda.getModelo().setRowCount(0);
        for (CitaDTO c : citasDelDia) {
            if (filtro != null && c.getIdTrabajador() != filtro.getId()) continue;
            citasVisibles.add(c);
            agenda.getModelo().addRow(new Object[] {
                c.getFechaHora().format(HORA),
                c.getNombrePaciente(),
                c.getNombreMedico(),
                c.getMotivoConsulta() != null ? c.getMotivoConsulta() : "",
                c.getEstado()
            });
        }
    }

    private void cancelarCitaSeleccionada() {
        int filaVista = agenda.getTabla().getSelectedRow();
        if (filaVista < 0) {
            info("Selecciona primero una cita de la agenda.");
            return;
        }
        CitaDTO cita = citasVisibles.get(agenda.getTabla().convertRowIndexToModel(filaVista));
        if (!"PENDIENTE".equals(cita.getEstado())) {
            info("Solo se pueden cancelar citas pendientes.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Cancelar la cita de " + cita.getNombrePaciente() + " a las " + cita.getFechaHora().format(HORA)
                + " con " + cita.getNombreMedico() + "?",
                "Cancelar cita", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        pedirAlServidor(new PaqueteRed(Acciones.CANCELAR_CITA, cita.getId()), r -> {
            cargarAgenda();
            recargarHuecos();   // el hueco vuelve a estar libre
        });
    }

    // =================================================================== PACIENTES

    private JPanel crearBarraPacientes() {
        JButton btnNuevo = new JButton("Nuevo paciente");
        Estilo.botonAccion(btnNuevo);
        btnNuevo.addActionListener(e -> nuevoPaciente());

        JButton btnModificar = new JButton("Modificar");
        btnModificar.setFont(Estilo.FUENTE);
        btnModificar.addActionListener(e -> modificarPaciente());

        JButton btnBaja = new JButton("Dar de baja");
        btnBaja.setFont(Estilo.FUENTE);
        btnBaja.addActionListener(e -> bajaPaciente());

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        barra.setOpaque(false);
        barra.add(btnBaja);
        barra.add(btnModificar);
        barra.add(btnNuevo);
        return barra;
    }

    private void buscarPacientes() {
        String texto = pacientes.getTxtBusqueda().getText().trim();
        this.<List<PacienteDTO>>pedirAlServidor(new PaqueteRed(Acciones.BUSCAR_PACIENTES, texto), lista -> {
            pacientesVisibles.clear();
            pacientesVisibles.addAll(lista);
            pacientes.getModelo().setRowCount(0);
            for (PacienteDTO p : lista) {
                pacientes.getModelo().addRow(new Object[] {
                    p.getDni(),
                    p.getNombre() + " " + p.getApellidos(),
                    p.getTelefono() != null ? p.getTelefono() : "",
                    p.getEmail() != null ? p.getEmail() : "",
                    p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FECHA) : ""
                });
            }
        });
    }

    private PacienteDTO pacienteSeleccionado() {
        int filaVista = pacientes.getTabla().getSelectedRow();
        if (filaVista < 0) {
            info("Selecciona primero un paciente de la lista.");
            return null;
        }
        return pacientesVisibles.get(pacientes.getTabla().convertRowIndexToModel(filaVista));
    }

    private void nuevoPaciente() {
        PacienteDTO datos = DialogoPaciente.mostrar(this, null);
        if (datos != null) guardarPaciente(datos, "Paciente dado de alta.");
    }

    private void modificarPaciente() {
        PacienteDTO p = pacienteSeleccionado();
        if (p == null) return;
        PacienteDTO datos = DialogoPaciente.mostrar(this, p);
        if (datos != null) guardarPaciente(datos, "Datos del paciente actualizados.");
    }

    private void guardarPaciente(PacienteDTO datos, String mensajeOk) {
        this.<PacienteDTO>pedirAlServidor(new PaqueteRed(Acciones.GUARDAR_PACIENTE, datos), guardado -> {
            info(mensajeOk);
            pacientes.getTxtBusqueda().setText(guardado.getDni());   // se muestra el paciente guardado
            buscarPacientes();
        });
    }

    private void bajaPaciente() {
        PacienteDTO p = pacienteSeleccionado();
        if (p == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a " + p.getNombre() + " " + p.getApellidos() + " (" + p.getDni() + ")?\n"
                + "Su historial se conserva y sus citas pendientes se cancelarán.",
                "Dar de baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        this.<String>pedirAlServidor(new PaqueteRed(Acciones.BAJA_PACIENTE, p.getId()), mensaje -> {
            info(mensaje);
            buscarPacientes();
            cargarAgenda();
        });
    }

    // =================================================================== REGISTRAR CITA

    private JPanel crearPantallaCita() {
        JPanel pantalla = new JPanel(new BorderLayout(0, 16));
        pantalla.setBackground(Estilo.FONDO);
        pantalla.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lblTitulo = new JLabel("Registrar cita");
        lblTitulo.setFont(Estilo.FUENTE_TITULO);
        lblTitulo.setForeground(Estilo.AZUL_OSCURO);
        JLabel lblSub = new JLabel("Elige paciente, médico y día: solo se ofrecen las horas libres del médico");
        lblSub.setFont(Estilo.FUENTE);
        lblSub.setForeground(Estilo.TEXTO_SUAVE);
        textos.add(lblTitulo);
        textos.add(lblSub);
        pantalla.add(textos, BorderLayout.NORTH);

        // --- controles ---
        textoCombo(cmbPaciente, p -> p.getNombre() + " " + p.getApellidos() + " · " + p.getDni(),
                "— busca un paciente —");
        textoCombo(cmbMedico, m -> m.getNombre() + " " + m.getApellidos()
                + (m.getEspecialidad() != null ? " · " + m.getEspecialidad() : ""), "— elige médico —");
        textoCombo(cmbHora, h -> h.format(HORA), "— sin horas libres —");
        for (JComponent c : new JComponent[] {cmbPaciente, cmbMedico, cmbHora, spnFecha}) {
            c.setFont(Estilo.FUENTE);
        }
        spnFecha.setEditor(new JSpinner.DateEditor(spnFecha, "dd/MM/yyyy"));
        lblHuecos.setFont(Estilo.FUENTE);
        lblHuecos.setForeground(Estilo.TEXTO_SUAVE);

        JButton btnBuscar = new JButton("Buscar");
        Estilo.botonAccion(btnBuscar);
        btnBuscar.addActionListener(e -> buscarPacientesCita());
        txtBuscarPacienteCita.addActionListener(e -> buscarPacientesCita());

        cmbMedico.addActionListener(e -> recargarHuecos());
        spnFecha.addChangeListener(e -> recargarHuecos());

        Estilo.botonAccion(btnRegistrarCita);
        btnRegistrarCita.addActionListener(e -> registrarCita());

        // --- formulario ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilo.BLANCO);
        form.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(20, 24, 20, 24)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JPanel buscador = new JPanel(new BorderLayout(10, 0));
        buscador.setOpaque(false);
        buscador.add(txtBuscarPacienteCita, BorderLayout.CENTER);
        buscador.add(btnBuscar, BorderLayout.EAST);

        int fila = 0;
        fila = filaFormulario(form, gbc, fila, "Buscar paciente (DNI o nombre)", buscador);
        fila = filaFormulario(form, gbc, fila, "Paciente", cmbPaciente);
        fila = filaFormulario(form, gbc, fila, "Médico", cmbMedico);
        fila = filaFormulario(form, gbc, fila, "Día", spnFecha);
        fila = filaFormulario(form, gbc, fila, "Hora", cmbHora);
        fila = filaFormulario(form, gbc, fila, "", lblHuecos);
        fila = filaFormulario(form, gbc, fila, "Motivo de la consulta", txtMotivo);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pie.setOpaque(false);
        pie.add(btnRegistrarCita);
        gbc.gridx = 1;
        gbc.gridy = fila;
        form.add(pie, gbc);

        // el formulario arriba, sin estirarse a toda la altura
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(form, BorderLayout.NORTH);
        pantalla.add(contenedor, BorderLayout.CENTER);
        return pantalla;
    }

    private static int filaFormulario(JPanel form, GridBagConstraints gbc, int fila, String titulo, Component campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(etiqueta(titulo), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(campo, gbc);
        return fila + 1;
    }

    private void cargarMedicos() {
        this.<List<TrabajadorDTO>>pedirAlServidor(new PaqueteRed(Acciones.LISTAR_MEDICOS), lista -> {
            cmbFiltroMedico.removeAllItems();
            cmbFiltroMedico.addItem(null);   // "Todos los médicos"
            cmbMedico.removeAllItems();
            for (TrabajadorDTO m : lista) {
                cmbFiltroMedico.addItem(m);
                cmbMedico.addItem(m);
            }
            cmbFiltroMedico.setSelectedIndex(0);
            recargarHuecos();
        });
    }

    private void buscarPacientesCita() {
        String texto = txtBuscarPacienteCita.getText().trim();
        if (texto.isEmpty()) {
            info("Escribe un DNI, nombre o apellidos para buscar al paciente.");
            return;
        }
        this.<List<PacienteDTO>>pedirAlServidor(new PaqueteRed(Acciones.BUSCAR_PACIENTES, texto), lista -> {
            cmbPaciente.removeAllItems();
            for (PacienteDTO p : lista) cmbPaciente.addItem(p);
            if (lista.isEmpty()) {
                info("No se ha encontrado ningún paciente. Puedes darlo de alta en «Pacientes».");
            }
        });
    }

    private LocalDate fechaElegida() {
        Date d = (Date) spnFecha.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Rellena el desplegable de horas con las franjas libres del médico ese día.
     * Pide la agenda del día al servidor y quita las horas ocupadas (las canceladas no ocupan).
     */
    private void recargarHuecos() {
        TrabajadorDTO medico = (TrabajadorDTO) cmbMedico.getSelectedItem();
        LocalDate dia = fechaElegida();
        cmbHora.removeAllItems();
        if (medico == null) {
            lblHuecos.setText(" ");
            return;
        }
        if (dia.isBefore(LocalDate.now())) {
            lblHuecos.setText("No se pueden dar citas en días pasados.");
            return;
        }
        lblHuecos.setText("Consultando horas libres…");

        this.<List<CitaDTO>>pedirAlServidor(new PaqueteRed(Acciones.AGENDA_CLINICA, dia), lista -> {
            // Si mientras tanto se ha cambiado de médico o de día, esta respuesta ya no sirve
            if (cmbMedico.getSelectedItem() != medico || !fechaElegida().equals(dia)) return;

            Set<LocalTime> ocupadas = new HashSet<>();
            for (CitaDTO c : lista) {
                if (c.getIdTrabajador() == medico.getId() && !"CANCELADA".equals(c.getEstado())) {
                    ocupadas.add(c.getFechaHora().toLocalTime());
                }
            }
            LocalDateTime ahora = LocalDateTime.now();
            cmbHora.removeAllItems();
            for (LocalTime h = APERTURA; h.isBefore(CIERRE); h = h.plusMinutes(15)) {
                if (!ocupadas.contains(h) && dia.atTime(h).isAfter(ahora)) {
                    cmbHora.addItem(h);
                }
            }
            lblHuecos.setText(cmbHora.getItemCount() == 0
                    ? "El médico no tiene horas libres ese día."
                    : cmbHora.getItemCount() + " horas libres.");
        });
    }

    private void registrarCita() {
        PacienteDTO paciente = (PacienteDTO) cmbPaciente.getSelectedItem();
        TrabajadorDTO medico = (TrabajadorDTO) cmbMedico.getSelectedItem();
        LocalTime hora = (LocalTime) cmbHora.getSelectedItem();
        if (paciente == null || medico == null || hora == null) {
            info("Elige paciente, médico y una hora libre.");
            return;
        }
        LocalDate dia = fechaElegida();
        CitaDTO nueva = CitaDTO.nueva(paciente.getId(), medico.getId(), dia.atTime(hora), txtMotivo.getText().trim());

        btnRegistrarCita.setEnabled(false);   // evita registrar la misma cita dos veces
        this.<CitaDTO>pedirAlServidor(new PaqueteRed(Acciones.CREAR_CITA, nueva),
                creada -> {
                    btnRegistrarCita.setEnabled(true);
                    info("Cita registrada: " + creada.getNombrePaciente() + " con " + creada.getNombreMedico()
                            + " el " + creada.getFechaHora().format(FECHA) + " a las "
                            + creada.getFechaHora().format(HORA) + ".");
                    txtMotivo.setText("");
                    recargarHuecos();
                    if (selectorDia.getDia().equals(dia)) cargarAgenda();
                },
                () -> {
                    btnRegistrarCita.setEnabled(true);
                    recargarHuecos();   // quizá otra recepcionista acaba de coger ese hueco
                });
    }

    // =================================================================== utilidades

    /** Cómo se escribe cada elemento de un desplegable (y qué se ve si no hay ninguno elegido). */
    @SuppressWarnings("unchecked")
    private static <T> void textoCombo(JComboBox<T> combo, Function<T, String> texto, String siVacio) {
        combo.setRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {
                String t = valor == null ? siVacio : texto.apply((T) valor);
                return super.getListCellRendererComponent(lista, t, indice, seleccionado, foco);
            }
        });
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.FUENTE_NEGRITA);
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static JTextField campoTexto() {
        JTextField t = new JTextField();
        t.setFont(Estilo.FUENTE);
        t.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(6, 10, 6, 10)));
        return t;
    }

    private void info(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "MedManager", JOptionPane.INFORMATION_MESSAGE);
    }
}
