package com.medmanager.enfermeriamedmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.medmanager.enfermeriamedmanager.red.ClienteSocket            // NUEVO
import com.medmanager.enfermeriamedmanager.red.ErrorServidorException   // NUEVO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Controles de la pantalla (se buscan en el XML por su id)
        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        btnEntrar.setOnClickListener {
            val usuario = etUsuario.text.toString().trim()   // convierte en String y quita espacios
            val password = etPassword.text.toString()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena usuario y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                btnEntrar.isEnabled = false   // evita pulsar dos veces mientras espera

                // Corrutina ligada a la pantalla: si se cierra, lifecycleScope la cancela sola
                lifecycleScope.launch {
                    try {
                        // La petición bloquea, así que va al hilo de E/S; al volver estamos otra vez en el principal
                        val trabajador = withContext(Dispatchers.IO) { ClienteSocket.login(usuario, password) }

                        if (trabajador.rol != "ENFERMERO") {
                            ClienteSocket.cerrar()   // no es enfermero: se cierra la sesión
                            Toast.makeText(this@MainActivity, "Esta app es solo para enfermería", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Bienvenido/a, ${trabajador.nombre}", Toast.LENGTH_SHORT).show()
                            // Pasa al buscador y cierra el login para que "atrás" no vuelva a él
                            startActivity(Intent(this@MainActivity, BuscadorActivity::class.java))
                            finish()
                        }
                    } catch (e: ErrorServidorException) {
                        // El servidor respondió ERROR (p. ej. contraseña incorrecta)
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                    } catch (e: IOException) {
                        // Fallo de red: servidor apagado, IP mal puesta…
                        Toast.makeText(this@MainActivity, "No se puede conectar con el servidor", Toast.LENGTH_LONG).show()
                    } finally {
                        btnEntrar.isEnabled = true
                    }
                }
            }
        }
    }
}
