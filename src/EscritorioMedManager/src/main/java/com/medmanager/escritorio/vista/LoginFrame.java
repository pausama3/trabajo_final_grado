package com.medmanager.escritorio.vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.medmanager.comun.TrabajadorDTO;
import com.medmanager.escritorio.red.ClienteSocket;
import com.medmanager.escritorio.red.ErrorServidorException;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtClave = new JPasswordField(16);
    private final JButton btnEntrar = new JButton("Entrar");
    private final JLabel lblMensaje = new JLabel(" ");

    public LoginFrame() {
        super("MedManager - Iniciar sesión");
        construirInterfaz();
        btnEntrar.addActionListener(e -> intentarLogin());
        getRootPane().setDefaultButton(btnEntrar);   // Enter = pulsar Entrar
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; panel.add(new JLabel("Usuario"), c);
        c.gridx = 1;              panel.add(txtUsuario, c);
        c.gridx = 0; c.gridy = 1; panel.add(new JLabel("Contraseña"), c);
        c.gridx = 1;              panel.add(txtClave, c);
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        panel.add(btnEntrar, c);
        c.gridy = 3;              panel.add(lblMensaje, c);

        setContentPane(panel);
    }

    private void intentarLogin() {
        final String usuario = txtUsuario.getText().trim();
        final char[] clave = txtClave.getPassword();
        if (usuario.isEmpty() || clave.length == 0) {
            lblMensaje.setText("Introduce usuario y contraseña");
            return;
        }
        btnEntrar.setEnabled(false);
        lblMensaje.setText("Conectando...");

        // La red va en otro hilo (SwingWorker) para que la ventana no se congele.
        new SwingWorker<TrabajadorDTO, Void>() {
            @Override
            protected TrabajadorDTO doInBackground() throws Exception {
                return ClienteSocket.getInstancia().login(usuario, new String(clave));
            }

            @Override
            protected void done() {   // vuelve a ejecutarse en el hilo de Swing
                Arrays.fill(clave, ' ');
                btnEntrar.setEnabled(true);
                try {
                    TrabajadorDTO t = get();
                    lblMensaje.setText(" ");
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Bienvenido/a " + t.getNombre() + " (" + t.getRol() + ")");
                    // Aquí abriremos el DashboardFrame
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    if (causa instanceof ErrorServidorException) {
                        lblMensaje.setText(causa.getMessage());
                    } else if (causa instanceof IOException) {
                        lblMensaje.setText("No se pudo conectar con el servidor");
                    } else {
                        lblMensaje.setText("Error inesperado: " + causa);
                    }
                    txtClave.setText("");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}