package com.medmanager.enfermeriamedmanager

import android.app.Activity
import android.content.Intent
import java.time.LocalDate
import java.time.Period

/** Funciones comunes a varias pantallas de la app. */

/** Edad en años cumplidos a día de hoy. */
fun edad(fechaNacimiento: LocalDate): Int = Period.between(fechaNacimiento, LocalDate.now()).years

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
