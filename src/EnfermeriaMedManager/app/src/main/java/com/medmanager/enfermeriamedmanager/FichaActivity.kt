package com.medmanager.enfermeriamedmanager

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.medmanager.comun.CitaDTO
import com.medmanager.comun.PacienteDTO
import com.medmanager.enfermeriamedmanager.red.ClienteSocket
import com.medmanager.enfermeriamedmanager.red.ErrorServidorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.format.DateTimeFormatter

/**
 * Pantalla 3 de la app de enfermería: ficha rápida del paciente (CU-E03).
 * - Identidad y alergias: llegan en el PacienteDTO del buscador (no hace falta pedirlas otra vez).
 * - Citas de hoy: se piden al servidor con CITAS_PACIENTE_HOY.
 */
class FichaActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PACIENTE = "paciente"
        private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
    }

    private lateinit var paciente: PacienteDTO
    private lateinit var llCitas: LinearLayout
    private lateinit var tvSinCitas: TextView
    private lateinit var pbCargando: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val recibido = IntentCompat.getSerializableExtra(intent, EXTRA_PACIENTE, PacienteDTO::class.java)
        if (ClienteSocket.usuarioActual == null || recibido == null) {
            volverAlLogin()   // sin sesión o sin paciente no hay nada que mostrar
            return
        }
        paciente = recibido

        enableEdgeToEdge()
        setContentView(R.layout.activity_ficha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        llCitas = findViewById(R.id.llCitas)
        tvSinCitas = findViewById(R.id.tvSinCitas)
        pbCargando = findViewById(R.id.pbCargando)
        findViewById<Button>(R.id.btnVolver).setOnClickListener { finish() }

        mostrarIdentidad()
        mostrarAlergias()
    }

    /** Se piden las citas cada vez que la pantalla vuelve a verse: así, al volver de la
     *  pantalla de observaciones, ya aparecen las que se acaban de guardar. */
    override fun onResume() {
        super.onResume()
        if (::paciente.isInitialized) cargarCitas()
    }

    private fun mostrarIdentidad() {
        findViewById<TextView>(R.id.tvNombre).text = "${paciente.nombre} ${paciente.apellidos}"
        findViewById<TextView>(R.id.tvDni).text = getString(R.string.ficha_dni, paciente.dni)

        val tvNacimiento = findViewById<TextView>(R.id.tvNacimiento)
        val nacimiento = paciente.fechaNacimiento
        if (nacimiento != null) {
            tvNacimiento.text = getString(R.string.ficha_nacimiento, nacimiento.format(FORMATO_FECHA), edad(nacimiento))
        } else {
            tvNacimiento.visibility = View.GONE
        }

        val telefono = paciente.telefono?.takeIf { it.isNotBlank() } ?: getString(R.string.sin_dato)
        findViewById<TextView>(R.id.tvTelefono).text = getString(R.string.ficha_telefono, telefono)
    }

    /** Las alergias son lo más importante para enfermería: recuadro rojo si tiene alguna. */
    private fun mostrarAlergias() {
        val card = findViewById<MaterialCardView>(R.id.cardAlergias)
        val tvTitulo = findViewById<TextView>(R.id.tvTituloAlergias)
        val tvAlergias = findViewById<TextView>(R.id.tvAlergias)

        val alergias = paciente.alergias?.trim().orEmpty()
        if (alergias.isNotEmpty()) {
            val rojo = ContextCompat.getColor(this, R.color.alerta)
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.alerta_fondo))
            card.strokeColor = rojo
            card.strokeWidth = resources.displayMetrics.density.toInt() * 2   // 2dp
            tvTitulo.setTextColor(rojo)
            tvAlergias.setTextColor(rojo)
            tvAlergias.text = alergias
        } else {
            tvTitulo.text = getString(R.string.titulo_sin_alergias)
            tvAlergias.text = getString(R.string.sin_alergias)
        }
    }

    private fun cargarCitas() {
        pbCargando.visibility = View.VISIBLE
        tvSinCitas.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val citas = withContext(Dispatchers.IO) { ClienteSocket.citasPacienteHoy(paciente.id) }
                mostrarCitas(citas)
            } catch (e: ErrorServidorException) {
                Toast.makeText(this@FichaActivity, e.message, Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                Toast.makeText(this@FichaActivity, R.string.error_conexion, Toast.LENGTH_LONG).show()
                ClienteSocket.cerrar()
                volverAlLogin()
            } finally {
                pbCargando.visibility = View.GONE
            }
        }
    }

    private fun mostrarCitas(citas: List<CitaDTO>) {
        llCitas.removeAllViews()
        tvSinCitas.visibility = if (citas.isEmpty()) View.VISIBLE else View.GONE

        val inflater = LayoutInflater.from(this)
        for (cita in citas) {
            val vista = inflater.inflate(R.layout.item_cita, llCitas, false)

            vista.findViewById<TextView>(R.id.tvHoraMedico).text =
                getString(R.string.cita_hora_medico, cita.fechaHora.format(FORMATO_HORA), cita.nombreMedico)

            // Estado con su color: PENDIENTE azul, REALIZADA verde, CANCELADA rojo
            val tvEstado = vista.findViewById<TextView>(R.id.tvEstado)
            tvEstado.text = cita.estado.lowercase().replaceFirstChar { it.uppercase() }
            val color = when (cita.estado) {
                "PENDIENTE" -> R.color.pendiente
                "REALIZADA" -> R.color.realizada
                else -> R.color.alerta
            }
            tvEstado.setTextColor(ContextCompat.getColor(this, color))

            val motivo = cita.motivoConsulta?.takeIf { it.isNotBlank() } ?: getString(R.string.sin_dato)
            vista.findViewById<TextView>(R.id.tvMotivo).text = getString(R.string.cita_motivo, motivo)

            val observaciones = cita.observacionesEnfermeria?.trim().orEmpty()
            vista.findViewById<TextView>(R.id.tvObservaciones).text =
                if (observaciones.isNotEmpty()) getString(R.string.cita_observaciones, observaciones)
                else getString(R.string.cita_sin_observaciones)

            // Solo se pueden anotar observaciones en citas pendientes (el servidor también lo comprueba)
            val btn = vista.findViewById<Button>(R.id.btnObservaciones)
            if (cita.estado == "PENDIENTE") {
                btn.setText(if (observaciones.isEmpty()) R.string.anadir_observaciones else R.string.editar_observaciones)
                btn.setOnClickListener { abrirObservaciones(cita) }
            } else {
                btn.visibility = View.GONE
            }

            llCitas.addView(vista)
        }
    }

    /** Pantalla 4: observaciones. La cita viaja en el Intent (CitaDTO es Serializable). */
    private fun abrirObservaciones(cita: CitaDTO) {
        startActivity(Intent(this, ObservacionesActivity::class.java)
            .putExtra(ObservacionesActivity.EXTRA_CITA, cita))
    }
}
