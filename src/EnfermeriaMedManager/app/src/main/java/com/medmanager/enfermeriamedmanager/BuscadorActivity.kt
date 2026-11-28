package com.medmanager.enfermeriamedmanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.medmanager.comun.PacienteDTO
import com.medmanager.enfermeriamedmanager.red.ClienteSocket
import com.medmanager.enfermeriamedmanager.red.ErrorServidorException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Pantalla 2 de la app de enfermería: búsqueda de pacientes por DNI, nombre o apellidos (CU-E02).
 * La red se usa con corrutinas: lifecycleScope las cancela solas si se cierra la pantalla.
 */
class BuscadorActivity : AppCompatActivity() {

    private companion object {
        const val MIN_CARACTERES = MIN_CARACTERES_BUSQUEDA   // definido en Utilidades.kt
        const val CLAVE_RESULTADOS = "resultados" // para no perder la lista al girar la pantalla
    }

    private lateinit var etBuscar: EditText
    private lateinit var btnBuscar: Button
    private lateinit var tvMensaje: TextView
    private lateinit var pbCargando: ProgressBar
    private lateinit var adapter: PacienteAdapter

    private var resultados: ArrayList<PacienteDTO>? = null   // null = todavía no se ha buscado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sin sesión (p. ej. Android cerró el proceso y lo ha recreado) no se puede usar el servidor
        val usuario = ClienteSocket.usuarioActual
        if (usuario == null) {
            volverAlLogin()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_buscador)
        val raiz = findViewById<View>(R.id.main)
        val margen = raiz.paddingLeft   // los 16dp del XML: se suman a las barras, no se sustituyen
        ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(barras.left + margen, barras.top + margen, barras.right + margen, barras.bottom + margen)
            insets
        }

        etBuscar = findViewById(R.id.etBuscar)
        btnBuscar = findViewById(R.id.btnBuscar)
        tvMensaje = findViewById(R.id.tvMensaje)
        pbCargando = findViewById(R.id.pbCargando)

        findViewById<TextView>(R.id.tvSaludo).text = getString(R.string.saludo, usuario.nombre)
        findViewById<Button>(R.id.btnCerrarSesion).setOnClickListener {
            ClienteSocket.cerrar()
            volverAlLogin()
        }

        adapter = PacienteAdapter { paciente -> abrirFicha(paciente) }
        findViewById<RecyclerView>(R.id.rvPacientes).apply {
            layoutManager = LinearLayoutManager(this@BuscadorActivity)
            adapter = this@BuscadorActivity.adapter
        }

        btnBuscar.setOnClickListener { buscar() }
        etBuscar.setOnEditorActionListener { _, accion, _ ->
            if (accion == EditorInfo.IME_ACTION_SEARCH) { buscar(); true } else false
        }

        // Si se ha girado la pantalla, se recuperan los resultados sin volver a pedirlos
        savedInstanceState?.let { estado ->
            @Suppress("UNCHECKED_CAST")
            val guardados = BundleCompat.getSerializable(estado, CLAVE_RESULTADOS, ArrayList::class.java)
                    as ArrayList<PacienteDTO>?
            if (guardados != null) mostrarResultados(guardados, etBuscar.text.toString().trim())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        resultados?.let { outState.putSerializable(CLAVE_RESULTADOS, it) }   // PacienteDTO es Serializable
    }

    private fun buscar() {
        val texto = etBuscar.text.toString().trim()
        if (!busquedaValida(texto)) {
            etBuscar.error = getString(R.string.buscar_minimo, MIN_CARACTERES)
            return
        }
        ocultarTeclado()
        cargando(true)

        lifecycleScope.launch {
            try {
                // La petición bloquea, así que va al hilo de E/S; al volver estamos otra vez en el principal
                val pacientes = withContext(Dispatchers.IO) { ClienteSocket.buscarPacientes(texto) }
                mostrarResultados(ArrayList(pacientes), texto)
            } catch (e: ErrorServidorException) {
                Toast.makeText(this@BuscadorActivity, e.message, Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                // ClienteSocket ya ha cerrado la conexión y, con ella, la sesión del servidor
                Toast.makeText(this@BuscadorActivity, R.string.error_conexion, Toast.LENGTH_LONG).show()
                ClienteSocket.cerrar()
                volverAlLogin()
            } finally {
                cargando(false)
            }
        }
    }

    private fun mostrarResultados(pacientes: ArrayList<PacienteDTO>, texto: String) {
        resultados = pacientes
        adapter.mostrar(pacientes)
        if (pacientes.isEmpty()) {
            tvMensaje.text = getString(R.string.sin_resultados, texto)
            tvMensaje.visibility = View.VISIBLE
        } else {
            tvMensaje.visibility = View.GONE
        }
    }

    private fun cargando(si: Boolean) {
        pbCargando.visibility = if (si) View.VISIBLE else View.GONE
        btnBuscar.isEnabled = !si   // evita lanzar dos búsquedas a la vez
        if (si) tvMensaje.visibility = View.GONE
    }

    /** Pantalla 3: ficha rápida. El paciente viaja en el Intent (PacienteDTO es Serializable). */
    private fun abrirFicha(paciente: PacienteDTO) {
        startActivity(Intent(this, FichaActivity::class.java).putExtra(FichaActivity.EXTRA_PACIENTE, paciente))
    }

    private fun ocultarTeclado() {
        val imm = getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(etBuscar.windowToken, 0)
    }
}
