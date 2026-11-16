package com.medmanager.escritorio.vista;

import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Barra "◀ Anterior · día · Hoy · Siguiente ▶" para las agendas.
 * Cada vez que cambia el día llama a 'alCambiar'. Se le pueden añadir más controles con add().
 */
public class SelectorDia extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter DIA_LARGO =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    private final JLabel lblDia = new JLabel();
    private final Runnable alCambiar;
    private LocalDate dia = LocalDate.now();

    public SelectorDia(Runnable alCambiar) {
        super(new FlowLayout(FlowLayout.CENTER, 10, 0));
        this.alCambiar = alCambiar;
        setOpaque(false);

        JButton btnAnterior = new JButton("◀ Anterior");
        JButton btnHoy = new JButton("Hoy");
        JButton btnSiguiente = new JButton("Siguiente ▶");
        for (JButton b : new JButton[] {btnAnterior, btnHoy, btnSiguiente}) {
            b.setFont(Estilo.FUENTE);
        }
        btnAnterior.addActionListener(e -> cambiar(dia.minusDays(1)));
        btnHoy.addActionListener(e -> cambiar(LocalDate.now()));
        btnSiguiente.addActionListener(e -> cambiar(dia.plusDays(1)));

        lblDia.setFont(Estilo.FUENTE_NEGRITA);
        lblDia.setForeground(Estilo.TEXTO);
        actualizarTexto();

        add(btnAnterior);
        add(lblDia);
        add(btnHoy);
        add(btnSiguiente);
    }

    public LocalDate getDia() {
        return dia;
    }

    private void cambiar(LocalDate nuevo) {
        dia = nuevo;
        actualizarTexto();
        alCambiar.run();
    }

    private void actualizarTexto() {
        String texto = dia.format(DIA_LARGO);
        lblDia.setText(Character.toUpperCase(texto.charAt(0)) + texto.substring(1));
    }
}
