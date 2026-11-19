package com.medmanager.escritorio.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import com.medmanager.comun.PaqueteRed;
import com.medmanager.comun.TrabajadorDTO;
import com.medmanager.escritorio.red.ClienteSocket;
import com.medmanager.escritorio.red.ErrorServidorException;

/**
 * Estructura común de las pantallas principales: cabecera, menú lateral y zona central.
 * Cada rol (DashboardMedico, DashboardRecepcion) hereda de esta clase y solo añade sus pantallas.
 * <p>
 * También ofrece {@link #pedirAlServidor(PaqueteRed, Consumer, Runnable)}, que
 * centraliza la comunicación asíncrona con el servidor y el tratamiento de errores.
 */
public abstract class DashboardBase extends JFrame {
    private static final long serialVersionUID = 1L;

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);   // aquí se cambia de pantalla
    private final JPanel opciones = new JPanel();          // los botones del menú
    private final List<JButton> botones = new ArrayList<>();

    /**
     * Construye el marco común de la ventana principal.
     *
     * @param usuario    trabajador que ha iniciado sesión (se muestra en la cabecera)
     * @param rolVisible texto del rol que se muestra bajo el nombre
     */
    protected DashboardBase(TrabajadorDTO usuario, String rolVisible) {
        super("MedManager");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 640));
        setSize(1100, 700);
        getContentPane().setLayout(new BorderLayout());

        contenido.setBackground(Estilo.FONDO);
        getContentPane().add(crearCabecera(usuario, rolVisible), BorderLayout.NORTH);
        getContentPane().add(crearMenu(), BorderLayout.WEST);
        getContentPane().add(contenido, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }

    /**
     * Las clases hijas llaman a este método: añade una pantalla y su botón en el menú.
     *
     * @param texto    texto del botón (también identifica la pantalla en el CardLayout)
     * @param pantalla panel que se muestra al pulsar el botón
     */
    protected void anadirPantalla(String texto, JPanel pantalla) {
        contenido.add(pantalla, texto);   // el texto del botón sirve de nombre de la carta
        JButton b = Estilo.botonMenu(texto);
        b.addActionListener(e -> mostrar(texto, b));
        botones.add(b);
        opciones.add(b);
        if (botones.size() == 1) {
            mostrar(texto, b);   // la primera pantalla añadida es la que se ve al abrir
        }
    }

    /**
     * Envía una petición al servidor en un hilo secundario (SwingWorker) para no congelar la ventana.
     * Si va bien, 'alRecibir' se ejecuta en el hilo de Swing con la respuesta.
     * Si falla, se muestra el error y se ejecuta 'siFalla' (puede ser null).
     *
     * @param <T>       tipo esperado del payload de la respuesta
     * @param peticion  paquete a enviar
     * @param alRecibir acción que recibe la respuesta
     * @param siFalla   acción opcional a ejecutar tras un error
     */
    @SuppressWarnings("unchecked")
    protected <T> void pedirAlServidor(PaqueteRed peticion, Consumer<T> alRecibir, Runnable siFalla) {
        new SwingWorker<Object, Void>() {
            @Override
            protected Object doInBackground() throws Exception {
                return ClienteSocket.getInstance().pedir(peticion);
            }

            @Override
            protected void done() {
                try {
                    // El tipo concreto lo fija cada acción del protocolo (ver Acciones)
                    alRecibir.accept((T) get());
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause();
                    if (causa instanceof ErrorServidorException) {
                        // El servidor ha respondido ERROR: su mensaje ya es legible
                        JOptionPane.showMessageDialog(DashboardBase.this, causa.getMessage(),
                                "No se pudo completar", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DashboardBase.this,
                                "No hay conexión con el servidor: " + causa.getMessage(),
                                "Error de conexión", JOptionPane.ERROR_MESSAGE);
                    }
                    if (siFalla != null) siFalla.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();   // se conserva el estado de interrupción
                }
            }
        }.execute();
    }

    /**
     * Variante sin acción de fallo: solo se muestra el mensaje de error.
     *
     * @param <T>       tipo esperado del payload de la respuesta
     * @param peticion  paquete a enviar
     * @param alRecibir acción que recibe la respuesta
     */
    protected <T> void pedirAlServidor(PaqueteRed peticion, Consumer<T> alRecibir) {
        pedirAlServidor(peticion, alRecibir, null);
    }

    /** Muestra la pantalla indicada y resalta su botón en el menú. */
    private void mostrar(String carta, JButton activo) {
        cartas.show(contenido, carta);
        for (JButton b : botones) {
            Estilo.marcarSeleccionado(b, b == activo);
        }
    }

    /** Cabecera superior: logo y marca a la izquierda; usuario y rol a la derecha. */
    private JPanel crearCabecera(TrabajadorDTO usuario, String rolVisible) {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(Estilo.BLANCO);
        cab.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, Estilo.LINEA), new EmptyBorder(10, 20, 10, 24)));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        izquierda.setOpaque(false);
        izquierda.add(new LogoPanel());
        JLabel lblMarca = new JLabel("MedManager");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblMarca.setForeground(Estilo.AZUL_OSCURO);
        izquierda.add(lblMarca);

        JLabel lblUsuario = new JLabel(usuario.getNombre() + " " + usuario.getApellidos());
        lblUsuario.setFont(Estilo.FUENTE_NEGRITA);
        lblUsuario.setForeground(Estilo.TEXTO);
        lblUsuario.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel lblRol = new JLabel(rolVisible);
        lblRol.setFont(Estilo.FUENTE.deriveFont(12f));
        lblRol.setForeground(Estilo.TEXTO_SUAVE);
        lblRol.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JPanel derecha = new JPanel();
        derecha.setOpaque(false);
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.add(lblUsuario);
        derecha.add(lblRol);

        cab.add(izquierda, BorderLayout.WEST);
        cab.add(derecha, BorderLayout.EAST);
        return cab;
    }

    /** Menú lateral: opciones de cada rol arriba y "Cerrar sesión" abajo. */
    private JPanel crearMenu() {
        JPanel menu = new JPanel(new BorderLayout());
        menu.setBackground(Estilo.AZUL_OSCURO);
        menu.setPreferredSize(new Dimension(220, 0));
        menu.setBorder(new EmptyBorder(20, 0, 20, 0));

        opciones.setLayout(new BoxLayout(opciones, BoxLayout.Y_AXIS));
        opciones.setOpaque(false);
        JLabel lblMenu = new JLabel("MENÚ");
        lblMenu.setFont(Estilo.FUENTE.deriveFont(Font.BOLD, 12f));
        lblMenu.setForeground(new Color(0x8FA3BF));
        lblMenu.setBorder(new EmptyBorder(0, 20, 12, 0));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        opciones.add(lblMenu);

        JButton btnSalir = Estilo.botonMenu("Cerrar sesión");
        btnSalir.addActionListener(e -> cerrarSesion());

        menu.add(opciones, BorderLayout.NORTH);   // opciones arriba
        menu.add(btnSalir, BorderLayout.SOUTH);   // cerrar sesión abajo
        return menu;
    }

    /** Cierra la sesión y vuelve a la ventana de inicio de sesión. */
    private void cerrarSesion() {
        ClienteSocket.getInstance().cerrar();   // cierra la conexión: el servidor olvida la sesión
        dispose();
        new LoginFrame().setVisible(true);
    }

    /** Logo dibujado con Java2D: un cuadrado redondeado con una cruz. */
    private static class LogoPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        LogoPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(34, 34));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Se trabaja sobre una copia del contexto para no alterar el original
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Estilo.ACENTO);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(Color.WHITE);
            int w = getWidth(), h = getHeight();
            g2.fillRoundRect(w / 2 - 3, h / 2 - 11, 6, 22, 4, 4);   // trazo vertical
            g2.fillRoundRect(w / 2 - 11, h / 2 - 3, 22, 6, 4, 4);   // trazo horizontal
            g2.dispose();
        }
    }
}