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
import java.util.function.Supplier;

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

/**
 * Pantalla principal de recepción: agenda de la clínica, gestión de pacientes y alta de citas.
 * <p>
 * Por la regla de negocio RN3, recepción nunca ve ni modifica las alergias de los pacientes.
 */
public class DashboardRecepcion extends DashboardBase {
    private static final long serialVersionUID = 1L;

    // ---- Formatos de fecha y hora para mostrar en pantalla ----
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Horario de la clínica para ofrecer huecos (franjas de 15 minutos, RN1)
    private static final LocalTime APERTURA = LocalTime.of(8, 0);
    private static final LocalTime CIERRE = LocalTime.of(20, 0);

    // ---- Agenda ----
    private final PanelBase agenda;
    private final SelectorDia selectorDia;
    private final JComboBox<TrabajadorDTO> cmbFiltroMedico = new JComboBox<>();   // elemento null = todos
    private List<CitaDTO> citasDelDia = new ArrayList<>();   // todas las citas del día recibidas del servidor
    private final List<CitaDTO> citasVisibles = new ArrayList<>();   // mismas posiciones que las filas

    // ---- Pacientes ----
    private final PanelBase pacientes;
    private final List<PacienteDTO> pacientesVisibles = new ArrayList<>();   // mismas posiciones que las filas

    // ---- Registrar cita ----
    private final JTextField txtBuscarPacienteCita = campoTexto();
    private final JComboBox<PacienteDTO> cmbPaciente = new JComboBox<>();
    private final JComboBox<TrabajadorDTO> cmbMedico = new JComboBox<>();
    private final JSpinner spnFecha = new JSpinner(new SpinnerDateModel());
    private final JComboBox<LocalTime> cmbHora = new JComboBox<>();
    private final JTextField txtMotivo = campoTexto();
    private final JLabel lblHuecos = new JLabel(" ");   // informa de cuántas horas libres hay
    private final JButton btnRegistrarCita = new JButton("Registrar cita");

    /**
     * Construye las tres secciones de recepción y lanza la carga inicial de datos.
     *
     * @param usuario recepcionista que ha iniciado sesión
     */
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

    /** Barra inferior de la agenda: selector de día, filtro por médico y acciones sobre la cita seleccionada. */
    private JPanel crearBarraAgenda() {
        cmbFiltroMedico.setFont(Estilo.FUENTE);
        textoCombo(cmbFiltroMedico, m -> m.getNombre() + " " + m.getApellidos(), "Todos los médicos");
        cmbFiltroMedico.addActionListener(e -> mostrarAgendaFiltrada());

        JButton btnCancelar = new JButton("Cancelar cita seleccionada");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> cancelarCitaSeleccionada());

        JButton btnReprogramar = new JButton("Reprogramar cita seleccionada");
        btnReprogramar.setFont(Estilo.FUENTE);
        btnReprogramar.addActionListener(e -> reprogramarCitaSeleccionada());

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        filtros.setOpaque(false);
        filtros.add(etiqueta("Médico:"));
        filtros.add(cmbFiltroMedico);
        filtros.add(btnReprogramar);
        filtros.add(btnCancelar);

        JPanel barra = new JPanel();
        barra.setOpaque(false);
        barra.setLayout(new BoxLayout(barra, BoxLayout.Y_AXIS));
        barra.add(selectorDia);
        barra.add(javax.swing.Box.createVerticalStrut(10));
        barra.add(filtros);
        return barra;
    }

    /** Pide al servidor todas las citas de la clínica del día seleccionado y las muestra. */
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

    /** La cita elegida en la agenda, o null (con aviso) si no hay ninguna o no está PENDIENTE. */
    /**
     * @param accion verbo que se usa en el aviso ("cancelar", "reprogramar")
     */
    private CitaDTO citaPendienteSeleccionada(String accion) {
        int filaVista = agenda.getTabla().getSelectedRow();
        if (filaVista < 0) {
            info("Selecciona primero una cita de la agenda.");
            return null;
        }
        CitaDTO cita = citasVisibles.get(agenda.getTabla().convertRowIndexToModel(filaVista));
        if (!"PENDIENTE".equals(cita.getEstado())) {
            info("Solo se pueden " + accion + " citas pendientes.");
            return null;
        }
        return cita;
    }

    /**
     * Reprogramar (RF2): se elige un nuevo día y una de las horas libres del mismo médico.
     * El servidor vuelve a comprobar la franja y MySQL rechaza el hueco si alguien lo ha cogido antes.
     */
    private void reprogramarCitaSeleccionada() {
        CitaDTO cita = citaPendienteSeleccionada("reprogramar");
        if (cita == null) return;

        // ---- Controles del diálogo: el día propuesto es el de la cita, o hoy si ya pasó ----
        LocalDate diaInicial = cita.getFechaHora().toLocalDate().isBefore(LocalDate.now())
                ? LocalDate.now() : cita.getFechaHora().toLocalDate();
        JSpinner spnNuevaFecha = new JSpinner(new SpinnerDateModel());
        spnNuevaFecha.setEditor(new JSpinner.DateEditor(spnNuevaFecha, "dd/MM/yyyy"));
        spnNuevaFecha.setValue(Date.from(diaInicial.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        JComboBox<LocalTime> cmbNuevaHora = new JComboBox<>();
        textoCombo(cmbNuevaHora, h -> h.format(HORA), "— sin horas libres —");
        JLabel lblLibres = new JLabel(" ");
        for (JComponent c : new JComponent[] {spnNuevaFecha, cmbNuevaHora, lblLibres}) {
            c.setFont(Estilo.FUENTE);
        }
        lblLibres.setForeground(Estilo.TEXTO_SUAVE);

        // Cada cambio de día vuelve a consultar las horas libres del mismo médico
        Runnable recargar = () -> cargarHorasLibres(cita.getIdTrabajador(), aLocalDate(spnNuevaFecha),
                cmbNuevaHora, lblLibres, () -> aLocalDate(spnNuevaFecha));
        spnNuevaFecha.addChangeListener(e -> recargar.run());
        recargar.run();

        // ---- Formulario del diálogo: datos actuales y nueva fecha/hora ----
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        int fila = 0;
        fila = filaFormulario(form, gbc, fila, "Paciente", new JLabel(cita.getNombrePaciente()));
        fila = filaFormulario(form, gbc, fila, "Médico", new JLabel(cita.getNombreMedico()));
        fila = filaFormulario(form, gbc, fila, "Cita actual",
                new JLabel(cita.getFechaHora().format(FECHA) + " a las " + cita.getFechaHora().format(HORA)));
        fila = filaFormulario(form, gbc, fila, "Nuevo día", spnNuevaFecha);
        fila = filaFormulario(form, gbc, fila, "Nueva hora", cmbNuevaHora);
        filaFormulario(form, gbc, fila, "", lblLibres);

        // Se reutiliza un JOptionPane como diálogo modal con botones Aceptar/Cancelar
        int ok = JOptionPane.showConfirmDialog(this, form, "Reprogramar cita",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        LocalTime hora = (LocalTime) cmbNuevaHora.getSelectedItem();
        if (hora == null) {
            info("No se ha elegido ninguna hora libre. La cita no se ha cambiado.");
            return;
        }
        LocalDateTime nueva = aLocalDate(spnNuevaFecha).atTime(hora);

        this.<CitaDTO>pedirAlServidor(new PaqueteRed(Acciones.REPROGRAMAR_CITA, CitaDTO.reprogramada(cita.getId(), nueva)),
                movida -> {
                    info("Cita reprogramada: " + movida.getNombrePaciente() + " con " + movida.getNombreMedico()
                            + " el " + movida.getFechaHora().format(FECHA) + " a las "
                            + movida.getFechaHora().format(HORA) + ".");
                    cargarAgenda();
                    recargarHuecos();   // el hueco antiguo queda libre
                },
                this::cargarAgenda);   // si otra recepcionista cogió el hueco, se refresca la agenda
    }

    /** Cancela, tras pedir confirmación, la cita pendiente seleccionada en la agenda. */
    private void cancelarCitaSeleccionada() {
        CitaDTO cita = citaPendienteSeleccionada("cancelar");
        if (cita == null) return;
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

    /** Barra inferior de la sección de pacientes con los botones de alta, modificación y baja. */
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

    /** Busca pacientes en el servidor con el texto del buscador y rellena la tabla (sin alergias, RN3). */
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

    /** El paciente elegido en la tabla, o null (con aviso) si no hay ninguno seleccionado. */
    private PacienteDTO pacienteSeleccionado() {
        int filaVista = pacientes.getTabla().getSelectedRow();
        if (filaVista < 0) {
            info("Selecciona primero un paciente de la lista.");
            return null;
        }
        return pacientesVisibles.get(pacientes.getTabla().convertRowIndexToModel(filaVista));
    }

    /** Abre el formulario vacío y, si se acepta, da de alta al paciente. */
    private void nuevoPaciente() {
        PacienteDTO datos = DialogoPaciente.mostrar(this, null);
        if (datos != null) guardarPaciente(datos, "Paciente dado de alta.");
    }

    /** Abre el formulario con los datos del paciente seleccionado y guarda los cambios. */
    private void modificarPaciente() {
        PacienteDTO p = pacienteSeleccionado();
        if (p == null) return;
        PacienteDTO datos = DialogoPaciente.mostrar(this, p);
        if (datos != null) guardarPaciente(datos, "Datos del paciente actualizados.");
    }

    /**
     * Envía el alta o la modificación al servidor y, si va bien, muestra el paciente guardado.
     *
     * @param datos     datos del formulario (id 0 = alta)
     * @param mensajeOk mensaje de confirmación para el usuario
     */
    private void guardarPaciente(PacienteDTO datos, String mensajeOk) {
        this.<PacienteDTO>pedirAlServidor(new PaqueteRed(Acciones.GUARDAR_PACIENTE, datos), guardado -> {
            info(mensajeOk);
            pacientes.getTxtBusqueda().setText(guardado.getDni());   // se muestra el paciente guardado
            buscarPacientes();
        });
    }

    /**
     * Da de baja, tras pedir confirmación, al paciente seleccionado. El servidor
     * conserva su historial y cancela sus citas pendientes.
     */
    private void bajaPaciente() {
        PacienteDTO p = pacienteSeleccionado();
        if (p == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "¿Dar de baja a " + p.getNombre() + " " + p.getApellidos() + " (" + p.getDni() + ")?\n"
                + "Su historial se conserva y sus citas pendientes se cancelarán.",
                "Dar de baja", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        this.<String>pedirAlServidor(new PaqueteRed(Acciones.BAJA_PACIENTE, p.getId()), mensaje -> {
            info(mensaje);   // resumen enviado por el servidor (p. ej., citas canceladas)
            buscarPacientes();
            cargarAgenda();  // las citas canceladas cambian de estado en la agenda
        });
    }

    // =================================================================== REGISTRAR CITA

    /** Construye la sección "Registrar cita": cabecera y formulario de alta de citas. */
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

        // Al cambiar de médico o de día se recalculan las horas libres
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

    /**
     * Añade una fila al formulario: etiqueta en la columna 0 y campo en la columna 1.
     *
     * @return la siguiente fila libre del GridBagLayout
     */
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

    /** Carga los médicos en el filtro de la agenda y en el desplegable de "Registrar cita". */
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

    /** Busca pacientes para la nueva cita y los carga en el desplegable de paciente. */
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

    /** Día seleccionado en el formulario de "Registrar cita". */
    private LocalDate fechaElegida() {
        return aLocalDate(spnFecha);
    }

    /** Convierte el {@link Date} de un JSpinner de fechas a {@link LocalDate} en la zona horaria local. */
    private static LocalDate aLocalDate(JSpinner spinner) {
        Date d = (Date) spinner.getValue();
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Rellena las horas libres del médico elegido en "Registrar cita". */
    private void recargarHuecos() {
        TrabajadorDTO medico = (TrabajadorDTO) cmbMedico.getSelectedItem();
        if (medico == null) {
            cmbHora.removeAllItems();
            lblHuecos.setText(" ");
            return;
        }
        // La respuesta solo es válida si, al llegar, sigue elegido el mismo médico
        cargarHorasLibres(medico.getId(), fechaElegida(), cmbHora, lblHuecos,
                () -> cmbMedico.getSelectedItem() == medico ? fechaElegida() : null);
    }

    /**
     * Rellena 'combo' con las franjas de 15 minutos libres de un médico en un día.
     * Pide la agenda del día al servidor y quita las horas ocupadas (las canceladas no ocupan).
     * 'diaVigente' devuelve el día que se está mirando ahora: si ha cambiado cuando llega la respuesta,
     * esa respuesta ya no sirve y se descarta.
     */
    private void cargarHorasLibres(int idMedico, LocalDate dia, JComboBox<LocalTime> combo, JLabel lbl,
            Supplier<LocalDate> diaVigente) {
        combo.removeAllItems();
        if (dia.isBefore(LocalDate.now())) {
            lbl.setText("No se pueden dar citas en días pasados.");
            return;
        }
        lbl.setText("Consultando horas libres…");

        this.<List<CitaDTO>>pedirAlServidor(new PaqueteRed(Acciones.AGENDA_CLINICA, dia), lista -> {
            if (!dia.equals(diaVigente.get())) return;   // respuesta obsoleta: el usuario ya cambió

            // Horas ya ocupadas por citas no canceladas de ese médico
            Set<LocalTime> ocupadas = new HashSet<>();
            for (CitaDTO c : lista) {
                if (c.getIdTrabajador() == idMedico && !"CANCELADA".equals(c.getEstado())) {
                    ocupadas.add(c.getFechaHora().toLocalTime());
                }
            }
            LocalDateTime ahora = LocalDateTime.now();
            combo.removeAllItems();
            // Se recorren todas las franjas del horario y se ofrecen las libres que aún no han pasado
            for (LocalTime h = APERTURA; h.isBefore(CIERRE); h = h.plusMinutes(15)) {
                if (!ocupadas.contains(h) && dia.atTime(h).isAfter(ahora)) {
                    combo.addItem(h);
                }
            }
            lbl.setText(combo.getItemCount() == 0
                    ? "El médico no tiene horas libres ese día."
                    : combo.getItemCount() + " horas libres.");
        });
    }

    /**
     * Valida el formulario y envía la nueva cita al servidor, que vuelve a comprobar
     * que la franja sigue libre antes de guardarla.
     */
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
                    recargarHuecos();   // la hora recién asignada deja de estar libre
                    if (selectorDia.getDia().equals(dia)) cargarAgenda();   // solo si la agenda muestra ese día
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

    /** Etiqueta en negrita con el estilo común de los formularios. */
    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Estilo.FUENTE_NEGRITA);
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    /** Campo de texto con el estilo común de los formularios. */
    private static JTextField campoTexto() {
        JTextField t = new JTextField();
        t.setFont(Estilo.FUENTE);
        t.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(6, 10, 6, 10)));
        return t;
    }

    /** Muestra un mensaje informativo al usuario. */
    private void info(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "MedManager", JOptionPane.INFORMATION_MESSAGE);
    }
}
