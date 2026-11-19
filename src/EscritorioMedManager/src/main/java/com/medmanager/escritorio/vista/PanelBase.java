package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Pantalla con título, buscador opcional y una tabla. Sirve de base para varias secciones.
 * <p>
 * Solo construye la interfaz: la carga de datos y la respuesta a los botones
 * las añade quien la utiliza, a través de los getters.
 */
public class PanelBase extends JPanel {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modelo;
    private final JTable tabla;
    private JTextField txtBusqueda;   // null si el panel se creó sin buscador
    private JButton btnBuscar;        // null si el panel se creó sin buscador

    /**
     * @param titulo      título grande de la sección
     * @param subtitulo   texto descriptivo bajo el título
     * @param columnas    cabeceras de la tabla
     * @param conBuscador si se muestra la barra de búsqueda
     */
    public PanelBase(String titulo, String subtitulo, String[] columnas, boolean conBuscador) {
        setLayout(new BorderLayout(0, 16));
        setBackground(Estilo.FONDO);
        setBorder(new EmptyBorder(24, 28, 24, 28));

        // ---- Cabecera: título, subtítulo y, opcionalmente, buscador ----
        JPanel cabecera = new JPanel(new BorderLayout(0, 14));
        cabecera.setOpaque(false);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Estilo.FUENTE_TITULO);
        lblTitulo.setForeground(Estilo.AZUL_OSCURO);
        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(Estilo.FUENTE);
        lblSub.setForeground(Estilo.TEXTO_SUAVE);
        textos.add(lblTitulo);
        textos.add(lblSub);
        cabecera.add(textos, BorderLayout.NORTH);

        if (conBuscador) {
            JPanel barra = new JPanel(new BorderLayout(10, 0));
            barra.setOpaque(false);
            txtBusqueda = new JTextField();
            txtBusqueda.setFont(Estilo.FUENTE);
            txtBusqueda.setBorder(new CompoundBorder(new LineBorder(Estilo.LINEA), new EmptyBorder(8, 12, 8, 12)));
            btnBuscar = new JButton("Buscar");
            Estilo.botonAccion(btnBuscar);
            barra.add(txtBusqueda, BorderLayout.CENTER);
            barra.add(btnBuscar, BorderLayout.EAST);
            cabecera.add(barra, BorderLayout.SOUTH);
        }
        add(cabecera, BorderLayout.NORTH);

        // ---- Tabla de resultados (de solo lectura) ----
        modelo = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int fila, int col) { return false; }
        };
        tabla = new JTable(modelo);
        Estilo.estilizarTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(new LineBorder(Estilo.LINEA));
        scroll.getViewport().setBackground(Estilo.BLANCO);
        add(scroll, BorderLayout.CENTER);
    }

    // Getters para que cada sección conecte sus datos y eventos
    public DefaultTableModel getModelo()   { return modelo; }
    public JTable getTabla()               { return tabla; }
    public JTextField getTxtBusqueda()     { return txtBusqueda; }
    public JButton getBtnBuscar()          { return btnBuscar; }
}