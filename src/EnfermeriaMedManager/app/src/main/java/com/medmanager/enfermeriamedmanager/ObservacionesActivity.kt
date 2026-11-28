package com.medmanager.enfermeriamedmanager

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.medmanager.comun.CitaDTO
import com.medmanager.enfermeriamedmanager.red.ClienteSocket
import com.medmanager.enfermeriamedmanager.red.ErrorServidorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.format.DateTimeFormatter

/**
 * Pantalla 4 de la app de enfermería: observaciones de una cita pendiente de hoy (CU-E04, RN4).
 * Enfermería solo puede escribir en este campo; el servidor comprueba que la cita sea pendiente y de hoy.
 * Al guardar se cierra y la ficha rápida recarga las citas en su onResume().
 */
class ObservacionesActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CITA = "cita"
        const val MAX_CARACTERES = MAX_CARACTERES_OBSERVACIONES   // definido en Utilidades.kt
        private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
    }

    private lateinit var cita: CitaDTO
    private lateinit var etObservaciones: TextInputEditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var pbGuardando: ProgressBar

    /** Texto con el que se abrió la pantalla: sirve para saber si hay cambios sin guardar. */
    private val textoOriginal: String get() = cita.observacionesEnfermeria?.trim().orEmpty()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recibida = IntentCompat.getSerializableExtra(intent, EXTRA_CITA, CitaDTO::class.java)
        if (ClienteSocket.usuarioActual == null || recibida == null) {
            volverAlLogin()
            return
        }
        cita = recibida

        enableEdgeToEdge()
        setContentView(R.layout.activity_observaciones)
        val raiz = findViewById<View>(R.id.main)
        val margen = raiz.paddingLeft   // los 16dp del XML se suman a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left + margen, barras.top + margen, barras.right + margen, barras.bottom + margen)
            insets
        }

        etObservaciones = findViewById(R.id.etObservaciones)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
        pbGuardando = findViewById(R.id.pbGuardando)

        mostrarResumen()

        // Solo la primera vez: al girar la pantalla el EditText ya recuerda lo que se estaba escribiendo
        if (savedInstanceState == null) {
            etObservaciones.setText(textoOriginal)
            etObservaciones.setSelection(etObservaciones.length())   // cursor al final para seguir escribiendo
        }

        btnGuardar.setOnClickListener { guardar() }
        btnCancelar.setOnClickListener { salir() }
        // El botón/gesto "atrás" del sistema también avisa si hay cambios sin guardar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = salir()
        })
    }

    /** Cabecera con la cita: paciente, alergias (aviso en rojo), hora, médico y motivo. */
    private fun mostrarResumen() {
        findViewById<TextView>(R.id.tvPaciente).text = cita.nombrePaciente
        findViewById<TextView>(R.id.tvHoraMedico).text =
            getString(R.string.cita_hora_medico, cita.fechaHora.format(FORMATO_HORA), cita.nombreMedico)

        val motivo = cita.motivoConsulta?.takeIf { it.isNotBlank() } ?: getString(R.string.sin_dato)
        findViewById<TextView>(R.id.tvMotivo).text = getString(R.string.cita_motivo, motivo)

        val alergias = cita.alergiasPaciente?.trim().orEmpty()
        val tvAlergias = findViewById<TextView>(R.id.tvAlergias)
        if (alergias.isNotEmpty()) {
            tvAlergias.text = getString(R.string.alergias, alergias)
        } else {
            tvAlergias.visibility = View.GONE
        }
    }

    private fun textoActual(): String = etObservaciones.text?.toString()?.trim().orEmpty()

    private fun guardar() {
        val texto = textoActual()
        if (!observacionesValidas(texto)) {   // el contador ya avisa, pero se comprueba igualmente
            etObservaciones.error = getString(R.string.observaciones_demasiado_largas, MAX_CARACTERES)
            return
        }
        if (texto == textoOriginal) {   // nada que guardar
            finish()
            return
        }
        guardando(true)

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) { ClienteSocket.guardarObservaciones(cita.id, texto) }
                val aviso = if (texto.isEmpty()) R.string.observaciones_borradas else R.string.observaciones_guardadas
                Toast.makeText(this@ObservacionesActivity, aviso, Toast.LENGTH_SHORT).show()
                finish()   // la ficha recarga las citas al volver a verse
            } catch (e: ErrorServidorException) {
                // p. ej. el médico ya ha registrado la consulta y la cita ha dejado de estar pendiente
                guardando(false)
                AlertDialog.Builder(this@ObservacionesActivity)
                    .setTitle(R.string.no_se_pudo_guardar)
                    .setMessage(e.message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
            } catch (e: IOException) {
                Toast.makeText(this@ObservacionesActivity, R.string.error_conexion, Toast.LENGTH_LONG).show()
                ClienteSocket.cerrar()
                volverAlLogin()
            }
        }
    }

    /** Cancelar o atrás: si hay cambios sin guardar, se pregunta antes de perderlos. */
    private fun salir() {
        if (!btnGuardar.isEnabled) return   // guardando: se espera a la respuesta del servidor
        if (textoActual() == textoOriginal) {
            finish()
            return
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.descartar_titulo)
            .setMessage(R.string.descartar_mensaje)
            .setPositiveButton(R.string.descartar) { _, _ -> finish() }
            .setNegativeButton(R.string.seguir_editando, null)
            .show()
    }

    private fun guardando(si: Boolean) {
        pbGuardando.visibility = if (si) View.VISIBLE else View.GONE
        btnGuardar.isEnabled = !si    // evita guardar dos veces
        btnCancelar.isEnabled = !si
        etObservaciones.isEnabled = !si
    }
}
