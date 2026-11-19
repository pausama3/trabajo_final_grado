package com.medmanager.escritorio;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.medmanager.escritorio.vista.LoginFrame;

/**
 * Punto de entrada de la aplicación de escritorio de MedManager.
 * <p>
 * Configura el aspecto visual y abre la ventana de inicio de sesión.
 */
public class Main {

    /**
     * Arranca la aplicación.
     *
     * @param args argumentos de línea de comandos (no se utilizan)
     */
    public static void main(String[] args) {
        try {   // aspecto del sistema (Windows), sin librerías externas
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // si falla, se queda el aspecto por defecto de Swing
        }
        // Swing no es thread-safe: la interfaz se crea en el hilo de eventos (EDT)
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}