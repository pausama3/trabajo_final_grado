package com.medmanager.escritorio.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Colores, fuentes y métodos para dar el mismo aspecto a toda la aplicación. */
public final class Estilo {
    private Estilo() { }

    public static final Color AZUL_OSCURO = new Color(0x1F3A5F);
    public static final Color AZUL_HOVER  = new Color(0x2C4F7C);
    public static final Color ACENTO      = new Color(0x2BB3A3);
    public static final Color FONDO       = new Color(0xF2F5F9);
    public static final Color BLANCO      = Color.WHITE;
    public static final Color TEXTO       = new Color(0x2B2F36);
    public static final Color TEXTO_SUAVE = new Color(0x6B7685);
    public static final Color LINEA       = new Color(0xDDE3EA);

    public static final Font FUENTE         = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FUENTE_NEGRITA = FUENTE.deriveFont(Font.BOLD);
    public static final Font FUENTE_TITULO  = new Font("Segoe UI", Font.BOLD, 24);

    /** Botón plano para el menú lateral, con efecto al pasar el ratón. */
    public static JButton botonMenu(String texto) {
        JButton b = new JButton(texto);
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(Color.WHITE);
        b.setBackground(AZUL_OSCURO);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);   // con estas dos líneas el color se ve en cualquier aspecto
        b.setOpaque(true);
        b.setBorder(bordeMenu(AZUL_OSCURO));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (!esSeleccionado(b)) b.setBackground(AZUL_HOVER);
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!esSeleccionado(b)) b.setBackground(AZUL_OSCURO);
            }
        });
        return b;
    }

    public static void marcarSeleccionado(JButton b, boolean seleccionado) {
        b.putClientProperty("seleccionado", seleccionado);
        b.setBackground(seleccionado ? AZUL_HOVER : AZUL_OSCURO);
        b.setBorder(bordeMenu(seleccionado ? ACENTO : AZUL_OSCURO));   // barra lateral de color
    }

    private static boolean esSeleccionado(JButton b) {
        return Boolean.TRUE.equals(b.getClientProperty("seleccionado"));
    }

    private static CompoundBorder bordeMenu(Color colorBarra) {
        return new CompoundBorder(new MatteBorder(0, 4, 0, 0, colorBarra), new EmptyBorder(12, 16, 12, 16));
    }

    /** Botón de acción (Buscar, Guardar...) con el color de acento. */
    public static void botonAccion(JButton b) {
        b.setFont(FUENTE_NEGRITA);
        b.setForeground(Color.WHITE);
        b.setBackground(ACENTO);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(8, 20, 8, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** Tabla sin rejilla, cabecera azul y filas alternas. */
    public static void estilizarTabla(JTable t) {
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFont(FUENTE);
        t.setForeground(TEXTO);
        t.setSelectionBackground(new Color(0xD7F1EE));
        t.setSelectionForeground(TEXTO);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                super.getTableCellRendererComponent(tabla, valor, sel, foco, fila, col);
                setBackground(AZUL_OSCURO);
                setForeground(Color.WHITE);
                setFont(FUENTE_NEGRITA);
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor,
                    boolean sel, boolean foco, int fila, int col) {
                Component c = super.getTableCellRendererComponent(tabla, valor, sel, false, fila, col);
                if (!sel) c.setBackground(fila % 2 == 0 ? BLANCO : new Color(0xF7F9FC));
                ((JComponent) c).setBorder(new EmptyBorder(0, 12, 0, 12));
                return c;
            }
        });
    }
}