package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.CitaDTO;
import com.medmanager.comun.HistorialDTO;
import com.medmanager.comun.InformeDTO;
import com.medmanager.comun.PacienteDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;

/**
 * Pantalla principal del médico: su agenda, sus pacientes y los informes. Todo viene del servidor.
 * <p>
 * Desde la agenda el médico puede registrar la consulta de una cita pendiente;
 * al guardar el informe, el servidor marca la cita como realizada.
 */
public class DashboardMedico extends DashboardBase {
    private static final long serialVersionUID = 1L;

    /** Índice de la columna "Acción" de la tabla de agenda. */
    private static final int COL_ACCION = 5;

    // ---- Formatos de fecha y hora para mostrar en las tablas ----
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PanelBase agenda;
    private final PanelBase pacientes;
    private final PanelBase informes;

    // Las citas que se ven en la agenda, en el mismo orden que las filas (así sabemos el id de cada fila)
    private List<CitaDTO> citasAgenda = new ArrayList<>();
    private final SelectorDia selectorDia;

    /**
     * Construye las tres secciones del médico y lanza la carga inicial de datos.
     *
     * @param usuario médico que ha iniciado sesión
     */
    public DashboardMedico(TrabajadorDTO usuario) {
        super(usuario, "Médico");

        // 1. Agenda del día (con navegación entre días)
        agenda = new PanelBase("Mi agenda", "Tus citas programadas, por día",
                new String[] {"Hora", "Paciente", "Motivo", "Observaciones de enfermería", "Estado", "Acción"}, false);
        selectorDia = new SelectorDia(this::cargarAgenda);
        agenda.add(selectorDia, BorderLayout.SOUTH);
        configurarColumnaAccion();
        anadirPantalla("Agenda", agenda);

        // 2. Pacientes (búsqueda en el servidor)
        pacientes = new PanelBase("Pacientes", "Busca por DNI, nombre o apellidos",
                new String[] {"DNI", "Nombre", "Teléfono", "Fecha de nacimiento", "Alergias"}, true);
        pacientes.getBtnBuscar().addActionListener(e -> buscarPacientes());
        pacientes.getTxtBusqueda().addActionListener(e -> buscarPacientes());   // Enter también busca
        anadirPantalla("Pacientes", pacientes);

        // 3. Informes: empieza vacío y se rellena al buscar un paciente
        informes = new PanelBase("Informes", "Busca un paciente por DNI, nombre o apellidos para ver su historial",
                new String[] {"Paciente", "Fecha", "Médico", "Diagnóstico", "Tratamiento"}, true);
        informes.getBtnBuscar().addActionListener(e -> buscarInformes());
        informes.getTxtBusqueda().addActionListener(e -> buscarInformes());   // Enter también busca
        anadirPantalla("Informes", informes);

        // Carga inicial
        cargarAgenda();
        buscarPacientes();
    }

    // =================================================================== AGENDA

    /** Pide al servidor las citas del médico para el día seleccionado y rellena la tabla. */
    private void cargarAgenda() {
        this.<List<CitaDTO>>pedirAlServidor(new PaqueteRed(Acciones.AGENDA_MEDICO, selectorDia.getDia()), lista -> {
            citasAgenda = lista;   // se guarda para saber qué cita corresponde a cada fila
            agenda.getModelo().setRowCount(0);   // se vacía la tabla antes de volver a llenarla
            for (CitaDTO c : lista) {
                agenda.getModelo().addRow(new Object[] {
                    c.getFechaHora().format(HORA),
                    c.getNombrePaciente(),
                    textoONada(c.getMotivoConsulta(), ""),
                    textoONada(c.getObservacionesEnfermeria(), ""),
                    c.getEstado(),
                    textoAccion(c.getEstado())
                });
            }
        });
    }

    /**
     * Texto de la columna "Acción" según el estado de la cita.
     * El renderizador convierte "registrar" en un botón.
     */
    private static String textoAccion(String estado) {
        switch (estado) {
            case "PENDIENTE": return "registrar";
            case "REALIZADA": return "registrada";
            default:          return "—";
        }
    }

    /** Convierte la columna "Acción" en un botón visual y escucha los clics. */
    private void configurarColumnaAccion() {
        JTable tabla = agenda.getTabla();
        Color filaPar = Estilo.BLANCO;
        Color filaImpar = new Color(0xF7F9FC);   // mismos colores alternos que Estilo.estilizarTabla

        // El botón solo se dibuja: los clics se detectan con el MouseListener de la tabla
        tabla.getColumnModel().getColumn(COL_ACCION).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                String texto = valor != null ? valor.toString() : "";
                Color fondo = sel ? t.getSelectionBackground() : (fila % 2 == 0 ? filaPar : filaImpar);

                if ("registrar".equals(texto)) {
                    JPanel contenedor = new JPanel(new BorderLayout());
                    contenedor.setBackground(fondo);
                    contenedor.setBorder(new EmptyBorder(4, 10, 4, 10));
                    JButton btn = new JButton("Registrar");
                    Estilo.botonAccion(btn);
                    btn.setBorder(new EmptyBorder(2, 10, 2, 10));
                    contenedor.add(btn, BorderLayout.CENTER);
                    return contenedor;
                }

                // Resto de estados: texto gris centrado
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, texto, sel, false, fila, col);
                lbl.setForeground(Estilo.TEXTO_SUAVE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(fondo);
                return lbl;
            }
        });

        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int filaVista = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (filaVista < 0 || col != COL_ACCION) return;   // clic fuera de la columna "Acción"

                // Se traduce la fila visible a la del modelo por si la tabla estuviese ordenada
                int fila = tabla.convertRowIndexToModel(filaVista);
                CitaDTO cita = citasAgenda.get(fila);
                if ("PENDIENTE".equals(cita.getEstado())) {   // solo las pendientes admiten informe
                    abrirDialogoInforme(cita);
                }
            }
        });
    }

    /** Ventana para redactar el informe. Al guardar, el servidor cierra la cita (REALIZADA). */
    private void abrirDialogoInforme(CitaDTO cita) {
        // Diálogo modal: bloquea el panel principal mientras se redacta el informe
        JDialog dialogo = new JDialog(this, "Registrar consulta — " + cita.getNombrePaciente(), true);
        dialogo.setSize(520, 640);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilo.FONDO);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Resumen superior de la cita (fila 0)
        String obs = textoONada(cita.getObservacionesEnfermeria(), "Sin observaciones");
        JLabel lblInfo = new JLabel("<html><b>Paciente:</b> " + cita.getNombrePaciente()
                + " (" + cita.getFechaHora().format(HORA) + ")<br>"
                + "<b>Motivo:</b> " + textoONada(cita.getMotivoConsulta(), "—") + "<br>"
                + "<b>Enfermería:</b> " + obs + "</html>");
        lblInfo.setFont(Estilo.FUENTE);
        lblInfo.setForeground(Estilo.TEXTO);
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(lblInfo, gbc);

        // Cada campo ocupa DOS filas (etiqueta + cuadro): por eso 1, 3 y 5
        JTextArea txtDiagnostico = anadirCampoTexto(form, gbc, 1, "Diagnóstico:", 0.4);
        JTextArea txtTratamiento = anadirCampoTexto(form, gbc, 3, "Tratamiento:", 0.4);
        JTextArea txtAlergias    = anadirCampoTexto(form, gbc, 5, "Alergias del paciente:", 0.2);
        txtAlergias.setText(textoONada(cita.getAlergiasPaciente(), ""));   // se ven las actuales y se pueden corregir

        // Botonera inferior
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pie.setBackground(Estilo.BLANCO);

        JButton btnCancelar = new JButton("Cerrar");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonAccion(btnGuardar);
        btnGuardar.addActionListener(e -> {
            // El diagnóstico es el único campo obligatorio para cerrar la consulta
            String diag = txtDiagnostico.getText().trim();
            if (diag.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Debes escribir un diagnóstico para cerrar la consulta.",
                        "Diagnóstico obligatorio", JOptionPane.WARNING_MESSAGE);
                return;
            }
            InformeDTO informe = new InformeDTO(cita.getId(), diag,
                    txtTratamiento.getText().trim(), txtAlergias.getText().trim());

            btnGuardar.setEnabled(false);   // evita enviar el informe dos veces
            pedirAlServidor(new PaqueteRed(Acciones.GUARDAR_INFORME, informe),
                    guardado -> {
                        dialogo.dispose();
                        cargarAgenda();     // la cita pasa a REALIZADA
                        if (!informes.getTxtBusqueda().getText().isBlank()) {
                            buscarInformes();   // si había una búsqueda abierta, se actualiza
                        }
                    },
                    () -> btnGuardar.setEnabled(true));   // si falla, se permite reintentar
        });

        pie.add(btnCancelar);
        pie.add(btnGuardar);

        dialogo.add(form, BorderLayout.CENTER);
        dialogo.add(pie, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }

    /** Añade al formulario una etiqueta (en 'fila') y debajo un cuadro de texto con scroll (en 'fila + 1'). */
    private JTextArea anadirCampoTexto(JPanel form, GridBagConstraints gbc, int fila, String titulo, double peso) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = fila;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        form.add(lbl, gbc);

        // 'peso' reparte el alto sobrante del diálogo entre los cuadros de texto
        JTextArea area = new JTextArea(3, 20);
        area.setFont(Estilo.FUENTE);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(new LineBorder(Estilo.LINEA));
        gbc.gridy = fila + 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = peso;
        form.add(scroll, gbc);
        return area;
    }

    // =================================================================== PACIENTES

    /**
     * Busca pacientes en el servidor con el texto del buscador y rellena la tabla.
     * Con el buscador vacío se listan todos (el servidor limita a 50). El médico sí ve las alergias.
     */
    private void buscarPacientes() {
        String texto = pacientes.getTxtBusqueda().getText().trim();
        this.<List<PacienteDTO>>pedirAlServidor(new PaqueteRed(Acciones.BUSCAR_PACIENTES, texto), lista -> {
            pacientes.getModelo().setRowCount(0);
            for (PacienteDTO p : lista) {
                pacientes.getModelo().addRow(new Object[] {
                    p.getDni(),
                    p.getNombre() + " " + p.getApellidos(),
                    textoONada(p.getTelefono(), ""),
                    p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FECHA) : "",
                    textoONada(p.getAlergias(), "Ninguna conocida")
                });
            }
        });
    }

    // =================================================================== INFORMES

    /**
     * Busca en el servidor los informes de los pacientes que coinciden con el texto
     * introducido. Si no hay texto, la tabla se deja vacía.
     */
    private void buscarInformes() {
        String texto = informes.getTxtBusqueda().getText().trim();
        informes.getModelo().setRowCount(0);
        if (texto.isEmpty()) {
            return;   // sin texto la tabla se queda en blanco
        }
        this.<List<HistorialDTO>>pedirAlServidor(new PaqueteRed(Acciones.BUSCAR_INFORMES, texto), lista -> {
            informes.getModelo().setRowCount(0);
            if (lista.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay informes para «" + texto + "».",
                        "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            }
            for (HistorialDTO h : lista) {
                informes.getModelo().addRow(new Object[] {
                    h.getNombrePaciente(),
                    h.getFechaRegistro() != null ? h.getFechaRegistro().format(FECHA_HORA) : "",
                    h.getNombreMedico(),
                    h.getDiagnostico(),
                    textoONada(h.getTratamiento(), "")
                });
            }
        });
    }

    // =================================================================== utilidades

    /**
     * Devuelve el texto o, si es nulo o está en blanco, el valor alternativo indicado.
     *
     * @param s       texto a comprobar
     * @param siVacio valor que se devuelve si {@code s} está vacío
     */
    private static String textoONada(String s, String siVacio) {
        return (s == null || s.isBlank()) ? siVacio : s;
    }
}
