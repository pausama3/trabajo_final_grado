package com.medmanager.escritorio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Comprobaciones de formato de los datos que se escriben en los formularios.
 * Están separadas de las pantallas para poder probarlas con JUnit.
 * Las reglas definitivas las sigue aplicando el servidor y la base de datos.
 */
public final class Validaciones {
    private Validaciones() { }   // clase de utilidades: no se instancia

    /** DNI: 8 números y una letra (no se comprueba que la letra sea la correcta). */
    private static final Pattern DNI = Pattern.compile("\\d{8}[A-Za-z]");
    /** NIE: X, Y o Z, 7 números y una letra. */
    private static final Pattern NIE = Pattern.compile("[XYZxyz]\\d{7}[A-Za-z]");
    /** Email: texto@dominio.extensión, sin espacios. */
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    /** Formato de fecha dd/mm/aaaa. STRICT rechaza fechas imposibles como el 31/02. */
    public static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    /**
     * @param texto lo escrito en el campo DNI / NIE
     * @return {@code true} si tiene el formato de un DNI o de un NIE
     */
    public static boolean esDniONie(String texto) {
        if (texto == null) return false;
        String t = texto.trim();
        return DNI.matcher(t).matches() || NIE.matcher(t).matches();
    }

    /**
     * @param texto lo escrito en el campo email
     * @return {@code true} si tiene el formato de una dirección de correo
     */
    public static boolean esEmail(String texto) {
        return texto != null && EMAIL.matcher(texto.trim()).matches();
    }

    /**
     * Lee una fecha escrita como dd/mm/aaaa.
     *
     * @param texto lo escrito en el campo fecha
     * @return la fecha, o {@code null} si el campo está vacío (es opcional)
     * @throws DateTimeParseException si el formato no es válido o la fecha no existe
     */
    public static LocalDate leerFecha(String texto) {
        if (texto == null || texto.isBlank()) return null;
        return LocalDate.parse(texto.trim(), FECHA);
    }
}
