package com.medmanager.enfermeriamedmanager

import android.app.Activity
import android.content.Intent
import java.time.LocalDate
import java.time.Period

/** Funciones comunes a varias pantallas de la app. */

/** Mínimo de caracteres para buscar: evita listar a todos los pacientes con 1 letra. */
const val MIN_CARACTERES_BUSQUEDA = 2

/** Máximo de caracteres de las observaciones: el mismo límite que MAX_OBSERVACIONES del servidor. */
const val MAX_CARACTERES_OBSERVACIONES = 2000

/** Edad en años cumplidos en la fecha [hoy] (por defecto, el día actual). */
fun edad(fechaNacimiento: LocalDate, hoy: LocalDate = LocalDate.now()): Int =
    Period.between(fechaNacimiento, hoy).years

/** Indica si el texto tiene suficientes caracteres para lanzar una búsqueda (sin contar los espacios de los extremos). */
fun busquedaValida(texto: String): Boolean = texto.trim().length >= MIN_CARACTERES_BUSQUEDA

/** Indica si las observaciones caben en el límite. Vacías se permiten: sirve para borrarlas. */
fun observacionesValidas(texto: String): Boolean = texto.trim().length <= MAX_CARACTERES_OBSERVACIONES

/**
 * Vuelve a la pantalla de login borrando las demás de la pila
 * (tras cerrar sesión o perder la conexión, no se debe poder volver atrás a ellas).
 */
fun Activity.volverAlLogin() {
    val intent = Intent(this, MainActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
    finish()
}
