package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import com.medmanager.comun.Acciones;
import com.medmanager.comun.LoginDTO;
import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;
import com.medmanager.escritorio.red.ClienteSocket;
import com.medmanager.escritorio.red.ErrorServidorException;

public class LoginFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextField textField_Usuario;
	private JPasswordField passwordField;
	private JLabel RespuestaLabel;
	private JButton btn_IniciarSesion;
	
	public LoginFrame() {
		setAlwaysOnTop(true);
		setTitle("Medmanager");
		setMinimumSize(new Dimension(600, 300));
		setLocationRelativeTo(null);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JLabel TituloLabel = new JLabel("Bienvenido a Medmanager");
		TituloLabel.setBounds(114, 16, 199, 20);
		panel.add(TituloLabel);
		
		textField_Usuario = new JTextField();
		textField_Usuario.setColumns(10);
		textField_Usuario.setBounds(199, 100, 146, 26);
		panel.add(textField_Usuario);
		
		JLabel UsuarioLabel = new JLabel("Usuario");
		UsuarioLabel.setBounds(94, 106, 63, 20);
		panel.add(UsuarioLabel);
		
		passwordField = new JPasswordField();
		passwordField.setColumns(10);
		passwordField.setBounds(199, 133, 146, 26);
		panel.add(passwordField);
		
		JLabel ContraseñaLabel = new JLabel("Contraseña");
		ContraseñaLabel.setBounds(94, 137, 85, 20);
		panel.add(ContraseñaLabel);
		
		
		
		btn_IniciarSesion = new JButton("Iniciar Sesion");
		btn_IniciarSesion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				intentarLogin();
			}
		});
		
		RespuestaLabel = new JLabel("");
		RespuestaLabel.setBounds(158, 180, 411, 20);
		panel.add(RespuestaLabel);
		btn_IniciarSesion.setBounds(155, 216, 158, 29);
		panel.add(btn_IniciarSesion);
	}  
	public void intentarLogin() {
	    String usuario = textField_Usuario.getText();
	    String clave = new String(passwordField.getPassword());
	    if (usuario.isEmpty() || clave.isEmpty()) {
	        RespuestaLabel.setText("Usuario o contraseña vacío");
	        return;
	    }
	    textField_Usuario.setEditable(false);
	    passwordField.setEditable(false);
	    btn_IniciarSesion.setEnabled(false);
	    RespuestaLabel.setText("Conectando...");

	    new SwingWorker<TrabajadorDTO, Void>() {
	        @Override
	        protected TrabajadorDTO doInBackground() throws Exception {
	            // OTRO HILO: aquí solo red, sin tocar ningún componente
	            PaqueteRed peticion = new PaqueteRed(Acciones.LOGIN, new LoginDTO(usuario, clave));
	            return ClienteSocket.getInstance().enviarPeticion(peticion);
	        }

	        @Override
	        protected void done() {
	            // HILO DE SWING: aquí sí se pueden tocar los componentes
	            try {
	                TrabajadorDTO t = get();   // el resultado de doInBackground
	                RespuestaLabel.setText("Bienvenido/a " + t.getNombre() + " (" + t.getRol() + ")");
	            } catch (ExecutionException e) {   // doInBackground lanzó una excepción
	                Throwable causa = e.getCause();
	                if (causa instanceof ErrorServidorException) {
	                    RespuestaLabel.setText(causa.getMessage());   // p. ej. "Usuario o contraseña incorrectos"
	                } else {
	                    RespuestaLabel.setText("Error de conexión con el servidor");
	                }
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	            }
	            textField_Usuario.setEditable(true);
	            passwordField.setEditable(true);
	            btn_IniciarSesion.setEnabled(true);
	        }
	    }.execute();
	}
}
