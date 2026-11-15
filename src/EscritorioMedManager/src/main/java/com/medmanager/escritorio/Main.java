package com.medmanager.escritorio;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.medmanager.escritorio.vista.LoginFrame;

public class Main {
    public static void main(String[] args) {
        try {   // aspecto del sistema (Windows), sin librerías externas
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // si falla, se queda el aspecto por defecto de Swing
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}