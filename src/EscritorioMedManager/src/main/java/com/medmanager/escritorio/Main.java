package com.medmanager.escritorio;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;
import com.medmanager.escritorio.vista.LoginFrame;

public class Main {
    public static void main(String[] args) {
        System.setProperty("flatlaf.useNativeLibrary", "false");   // quita el aviso rojo
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}