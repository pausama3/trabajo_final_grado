package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.medmanager.comun.PacienteDTO;

/**
 * Formulario de alta / modificación de los datos personales de un paciente.
 * Uso: PacienteDTO datos = DialogoPaciente.mostrar(ventana, pacienteOnull);  // null = ha cancelado
 * No incluye alergias: recepción no las ve ni las cambia (RN3).
 */
public class DialogoPaciente extends JDialog {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JTextField txtDni = campo();
    private final JTextField txtNombre = campo();
    private final JTextField txtApellidos = campo();
    private final JTextField txtTelefono = campo();
    private final JTextField txtEmail = campo();
    private final JTextField txtDireccion = campo();
    private final JTextField txtFechaNac = campo();

    private final int idPaciente;    // 0 = alta
    private PacienteDTO resultado;   // se rellena al pulsar Guardar

    public static PacienteDTO mostrar(Window padre, PacienteDTO paciente) {
        DialogoPaciente d = new DialogoPaciente(padre, paciente);
        d.setVisible(true);   // modal: se queda aquí hasta que se cierra
        return d.resultado;
    }

    private DialogoPaciente(Window padre, PacienteDTO p) {
        super(padre, p == null ? "Nuevo paciente" : "Modificar paciente", ModalityType.APPLICATION_MODAL);
        idPaciente = p == null ? 0 : p.getId();
        setSize(460, 520);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Estilo.FONDO);
        form.setBorder(new EmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        int fila = 0;
        fila = anadir(form, gbc, fila, "DNI / NIE *", txtDni);
        fila = anadir(form, gbc, fila, "Nombre *", txtNombre);
        fila = anadir(form, gbc, fila, "Apellidos *", txtApellidos);
        fila = anadir(form, gbc, fila, "Teléfono", txtTelefono);
        fila = anadir(form, gbc, fila, "Email", txtEmail);
        fila = anadir(form, gbc, fila, "Dirección", txtDireccion);
        anadir(form, gbc, fila, "Fecha de nacimiento (dd/mm/aaaa)", txtFechaNac);

        if (p != null) {
            txtDni.setText(p.getDni());
            txtNombre.setText(p.getNombre());
            txtApellidos.setText(p.getApellidos());
            txtTelefono.setText(nulo(p.getTelefono()));
            txtEmail.setText(nulo(p.getEmail()));
            txtDireccion.setText(nulo(p.getDireccion()));
            txtFechaNac.setText(p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FECHA) : "");
        }

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pie.setBackground(Estilo.BLANCO);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> dispose());
        JButton btnGuardar = new JButton("Guardar");
        Estilo.botonAccion(btnGuardar);
        btnGuardar.addActionListener(e -> guardar());
        pie.add(btnCancelar);
        pie.add(btnGuardar);
        getRootPane().setDefaultButton(btnGuardar);   // Enter = Guardar

        add(form, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    /** Comprobaciones básicas aquí; las definitivas las hace el servidor. */
    private void guardar() {
        if (txtDni.getText().isBlank() || txtNombre.getText().isBlank() || txtApellidos.getText().isBlank()) {
            aviso("DNI, nombre y apellidos son obligatorios.");
            return;
        }
        LocalDate fechaNac = null;
        if (!txtFechaNac.getText().isBlank()) {
            try {
                fechaNac = LocalDate.parse(txtFechaNac.getText().trim(), FECHA);
            } catch (DateTimeParseException ex) {
                aviso("La fecha de nacimiento debe tener el formato dd/mm/aaaa.");
                return;
            }
        }
        resultado = new PacienteDTO(idPaciente,
                txtDni.getText().trim().toUpperCase(), txtNombre.getText().trim(), txtApellidos.getText().trim(),
                txtTelefono.getText().trim(), txtEmail.getText().trim(), txtDireccion.getText().trim(),
                fechaNac, null, true);
        dispose();
    }

    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    private static int anadir(JPanel form, GridBagConstraints gbc, int fila, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = fila;
        form.add(lbl, gbc);
        gbc.gridy = fila + 1;
        form.add(campo, gbc);
        return fila + 2;
    }

    private static JTextField campo() {
        JTextField t = new JTextField();
        t.setFont(Estilo.FUENTE);
        t.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(6, 10, 6, 10)));
        return t;
    }

    private static String nulo(String s) {
        return s == null ? "" : s;
    }
}
