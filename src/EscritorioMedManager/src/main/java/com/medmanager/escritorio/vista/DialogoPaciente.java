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
import com.medmanager.escritorio.Validaciones;

/**
 * Formulario de alta / modificación de los datos personales de un paciente.
 * Uso: PacienteDTO datos = DialogoPaciente.mostrar(ventana, pacienteOnull);  // null = ha cancelado
 * No incluye alergias: recepción no las ve ni las cambia (RN3).
 */
public class DialogoPaciente extends JDialog {
    private static final long serialVersionUID = 1L;

    /** Formato de fecha que se muestra y se acepta en el formulario. */
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ---- Campos del formulario ----
    private final JTextField txtDni = campo();
    private final JTextField txtNombre = campo();
    private final JTextField txtApellidos = campo();
    private final JTextField txtTelefono = campo();
    private final JTextField txtEmail = campo();
    private final JTextField txtDireccion = campo();
    private final JTextField txtFechaNac = campo();

    private final int idPaciente;    // 0 = alta
    private PacienteDTO resultado;   // se rellena al pulsar Guardar

    /**
     * Muestra el formulario de forma modal y espera a que el usuario lo cierre.
     *
     * @param padre    ventana sobre la que se centra el diálogo
     * @param paciente paciente a modificar, o {@code null} para un alta nueva
     * @return los datos introducidos, o {@code null} si el usuario canceló
     */
    public static PacienteDTO mostrar(Window padre, PacienteDTO paciente) {
        DialogoPaciente d = new DialogoPaciente(padre, paciente);
        d.setVisible(true);   // modal: se queda aquí hasta que se cierra
        return d.resultado;
    }

    /** Constructor privado: el diálogo se abre siempre con {@link #mostrar(Window, PacienteDTO)}. */
    private DialogoPaciente(Window padre, PacienteDTO p) {
        super(padre, p == null ? "Nuevo paciente" : "Modificar paciente", ModalityType.APPLICATION_MODAL);
        idPaciente = p == null ? 0 : p.getId();
        setSize(460, 520);
        setLocationRelativeTo(padre);
        setLayout(new BorderLayout());

        // ---- Formulario: una etiqueta encima de cada campo ----
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

        // En modo modificación se precargan los datos actuales
        if (p != null) {
            txtDni.setText(p.getDni());
            txtNombre.setText(p.getNombre());
            txtApellidos.setText(p.getApellidos());
            txtTelefono.setText(nulo(p.getTelefono()));
            txtEmail.setText(nulo(p.getEmail()));
            txtDireccion.setText(nulo(p.getDireccion()));
            txtFechaNac.setText(p.getFechaNacimiento() != null ? p.getFechaNacimiento().format(FECHA) : "");
        }

        // ---- Pie con los botones ----
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pie.setBackground(Estilo.BLANCO);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(Estilo.FUENTE);
        btnCancelar.addActionListener(e -> dispose());   // resultado queda a null
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
        if (!Validaciones.esDniONie(txtDni.getText())) {
            aviso("El DNI debe tener 8 números y una letra (o el NIE: X, Y o Z, 7 números y una letra).");
            return;
        }
        // El email es opcional, pero si se escribe debe tener formato de correo
        if (!txtEmail.getText().isBlank() && !Validaciones.esEmail(txtEmail.getText())) {
            aviso("El email no tiene un formato válido.");
            return;
        }
        // La fecha de nacimiento es opcional, pero si se escribe debe ser válida
        LocalDate fechaNac;
        try {
            fechaNac = Validaciones.leerFecha(txtFechaNac.getText());
        } catch (DateTimeParseException ex) {
            aviso("La fecha de nacimiento debe tener el formato dd/mm/aaaa y ser una fecha que exista.");
            return;
        }
        // El DNI se normaliza en mayúsculas; las alergias van a null porque recepción no las gestiona
        resultado = new PacienteDTO(idPaciente,
                txtDni.getText().trim().toUpperCase(), txtNombre.getText().trim(), txtApellidos.getText().trim(),
                txtTelefono.getText().trim(), txtEmail.getText().trim(), txtDireccion.getText().trim(),
                fechaNac, null, true);
        dispose();
    }

    /** Muestra un aviso de validación sin cerrar el formulario. */
    private void aviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Añade al formulario una etiqueta y, debajo, su campo de texto.
     *
     * @return la siguiente fila libre del GridBagLayout
     */
    private static int anadir(JPanel form, GridBagConstraints gbc, int fila, String titulo, JTextField campo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Estilo.FUENTE_NEGRITA);
        gbc.gridy = fila;
        form.add(lbl, gbc);
        gbc.gridy = fila + 1;
        form.add(campo, gbc);
        return fila + 2;
    }

    /** Crea un campo de texto con el estilo común de la aplicación. */
    private static JTextField campo() {
        JTextField t = new JTextField();
        t.setFont(Estilo.FUENTE);
        t.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(6, 10, 6, 10)));
        return t;
    }

    /** Convierte {@code null} en cadena vacía para poder mostrarlo en un campo de texto. */
    private static String nulo(String s) {
        return s == null ? "" : s;
    }
}
