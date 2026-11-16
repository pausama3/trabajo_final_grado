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

import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.medmanager.comun.TrabajadorDTO;

/** Pantalla principal del médico: su agenda, sus pacientes y los informes. */
public class DashboardMedico extends DashboardBase {
    private static final long serialVersionUID = 1L;

    private final PanelBase agenda;
    private final PanelBase informes;

    public DashboardMedico(TrabajadorDTO usuario) {
        super(usuario, "Médico");

        // 1. Panel de Agenda con la 6ª columna "Accion"
        agenda = new PanelBase("Mi agenda", "Tus citas programadas, por día",
                new String[] {"Hora", "Paciente", "Motivo", "Observaciones de enfermería", "Estado", "Accion"}, false);

        // DATOS DE EJEMPLO: se borrarán cuando conectemos el servidor
        agenda.getModelo().addRow(new Object[] {"09:00", "Ana Pérez López", "Revisión anual", "Sin observaciones", "PENDIENTE", "registrar"});
        agenda.getModelo().addRow(new Object[] {"09:15", "Pedro Sanz Molina", "Dolor de espalda", "Tensión 13/8", "PENDIENTE", "registrar"});
        agenda.getModelo().addRow(new Object[] {"09:30", "Lucía Marín Ortiz", "Control de tensión", "", "REALIZADA", "registrada"});

        configurarColumnaAccion();
        anadirPantalla("Agenda", agenda);

        // 2. Panel de Pacientes
        anadirPantalla("Pacientes", new PanelBase("Pacientes", "Busca por DNI, nombre o apellidos",
                new String[] {"DNI", "Nombre", "Teléfono", "Próxima cita", "Alergias"}, true));

        // 3. Panel de Informes
        informes = new PanelBase("Informes", "Pacientes asignados a ti y sus informes",
                new String[] {"Paciente", "Fecha", "Diagnóstico", "Tratamiento"}, true);
        anadirPantalla("Informes", informes);
    }

    /** Convierte la columna "Accion" (índice 5) en un botón visual y escucha los clics. */
    private void configurarColumnaAccion() {
        JTable tabla = agenda.getTabla();
        int colAccion = 5; // Índice de la columna "Accion"

        // Renderizador: dibuja un botón si pone "registrar", o texto gris si pone "registrada"
        tabla.getColumnModel().getColumn(colAccion).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable t, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                String texto = valor != null ? valor.toString() : "";

                if ("registrar".equalsIgnoreCase(texto)) {
                    JPanel contenedor = new JPanel(new BorderLayout());
                    contenedor.setBackground(sel ? t.getSelectionBackground() : (fila % 2 == 0 ? Estilo.BLANCO : new java.awt.Color(0xF7F9FC)));
                    contenedor.setBorder(new EmptyBorder(4, 10, 4, 10));

                    JButton btn = new JButton("Registrar");
                    Estilo.botonAccion(btn);
                    btn.setBorder(new EmptyBorder(2, 10, 2, 10));
                    contenedor.add(btn, BorderLayout.CENTER);
                    return contenedor;
                }

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, texto, sel, false, fila, col);
                lbl.setForeground(Estilo.TEXTO_SUAVE);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(sel ? t.getSelectionBackground() : (fila % 2 == 0 ? Estilo.BLANCO : new java.awt.Color(0xF7F9FC)));
                return lbl;
            }
        });

        // Escuchador de clics sobre la columna "Accion"
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());

                if (fila >= 0 && col == colAccion) {
                    String accion = String.valueOf(tabla.getValueAt(fila, colAccion));
                    if ("registrar".equalsIgnoreCase(accion)) {
                        abrirDialogoInforme(fila);
                    }
                }
            }
        });
    }

    /** Abre la ventana emergente para redactar el informe y cambiar el estado de la cita. */
    private void abrirDialogoInforme(int fila) {
        String hora = String.valueOf(agenda.getModelo().getValueAt(fila, 0));
        String paciente = String.valueOf(agenda.getModelo().getValueAt(fila, 1));
        String motivo = String.valueOf(agenda.getModelo().getValueAt(fila, 2));
        String obsEnfermeria = String.valueOf(agenda.getModelo().getValueAt(fila, 3));

        JDialog dialogo = new JDialog(this, "Registrar consulta — " + paciente, true);
        dialogo.setSize(520, 470);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilo.FONDO);
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Resumen superior de la cita
        JLabel lblInfo = new JLabel("<html><b>Paciente:</b> " + paciente + " (" + hora + ")<br>"
                + "<b>Motivo:</b> " + motivo + "<br>"
                + "<b>Enfermería:</b> " + (obsEnfermeria.isEmpty() ? "Sin observaciones" : obsEnfermeria) + "</html>");
        lblInfo.setFont(Estilo.FUENTE);
        lblInfo.setForeground(Estilo.TEXTO);
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(lblInfo, gbc);

        // Desplegable de Estado (REALIZADA o CANCELADA)
        JLabel lblEstado = new JLabel("Nuevo estado de la cita:");
        lblEstado.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = 1;
        form.add(lblEstado, gbc);

        JComboBox<String> cmbEstado = new JComboBox<>(new String[] {"REALIZADA", "CANCELADA"});
        cmbEstado.setFont(Estilo.FUENTE);
        gbc.gridy = 2;
        form.add(cmbEstado, gbc);

        // Campo Diagnóstico
        JLabel lblDiag = new JLabel("Diagnóstico:");
        lblDiag.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = 3;
        form.add(lblDiag, gbc);

        JTextArea txtDiagnostico = new JTextArea(3, 20);
        txtDiagnostico.setFont(Estilo.FUENTE);
        txtDiagnostico.setLineWrap(true);
        txtDiagnostico.setWrapStyleWord(true);
        JScrollPane scrollDiag = new JScrollPane(txtDiagnostico);
        scrollDiag.setBorder(new LineBorder(Estilo.LINEA));
        gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.5;
        form.add(scrollDiag, gbc);

        // Campo Tratamiento
        JLabel lblTrat = new JLabel("Tratamiento:");
        lblTrat.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;
        form.add(lblTrat, gbc);

        JTextArea txtTratamiento = new JTextArea(3, 20);
        txtTratamiento.setFont(Estilo.FUENTE);
        txtTratamiento.setLineWrap(true);
        txtTratamiento.setWrapStyleWord(true);
        JScrollPane scrollTrat = new JScrollPane(txtTratamiento);
        scrollTrat.setBorder(new LineBorder(Estilo.LINEA));
        gbc.gridy = 6; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.5;
        form.add(scrollTrat, gbc);

        // Si elige CANCELADA, deshabilitamos los cuadros de texto del informe
        cmbEstado.addActionListener(e -> {
            boolean esRealizada = "REALIZADA".equals(cmbEstado.getSelectedItem());
            txtDiagnostico.setEnabled(esRealizada);
            txtTratamiento.setEnabled(esRealizada);
        });

        // Botonera inferior
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pie.setBackground(Estilo.BLANCO);

        JButton btnCancelar = new JButton("Cerrar");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> dialogo.dispose());

        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonAccion(btnGuardar);
        btnGuardar.addActionListener(e -> {
            String estadoElegido = (String) cmbEstado.getSelectedItem();
            String diag = txtDiagnostico.getText().trim();
            String trat = txtTratamiento.getText().trim();

            if ("REALIZADA".equals(estadoElegido) && diag.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Debes escribir un diagnóstico para marcar la cita como REALIZADA.",
                        "Diagnóstico obligatorio", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 1. Actualizar fila en la tabla de Agenda
            agenda.getModelo().setValueAt(estadoElegido, fila, 4);
            agenda.getModelo().setValueAt("registrada", fila, 5);

            // 2. Si se ha realizado, añadir el informe a la pestaña "Informes"
            if ("REALIZADA".equals(estadoElegido)) {
                informes.getModelo().addRow(new Object[] {
                    paciente, LocalDate.now().toString(), diag, trat
                });
            }

            // AQUÍ: Cuando conectes el servidor, enviarás el PaqueteRed con SwingWorker
            dialogo.dispose();
        });

        pie.add(btnCancelar);
        pie.add(btnGuardar);

        dialogo.add(form, BorderLayout.CENTER);
        dialogo.add(pie, BorderLayout.SOUTH);
        dialogo.setVisible(true);
    }
}