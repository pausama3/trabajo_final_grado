package com.medmanager.enfermeriamedmanager

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.medmanager.enfermeriamedmanager.red.ClienteSocket            // NUEVO
import com.medmanager.enfermeriamedmanager.red.ErrorServidorException   // NUEVO

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
                // NUEVO: login contra el servidor
                btnEntrar.isEnabled = false   // evita pulsar dos veces mientras espera

                Thread {
                    // Hilo secundario: aquí SÍ se puede usar la red
                    try {
                        val trabajador = ClienteSocket.login(usuario, password)

                        runOnUiThread {
                            // Hilo principal: aquí SÍ se puede tocar la pantalla
                            btnEntrar.isEnabled = true
                            if (trabajador.rol != "ENFERMERO") {
                                ClienteSocket.cerrar()   // no es enfermero: se cierra la sesión
                                Toast.makeText(this, "Esta app es solo para enfermería", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Bienvenido/a, ${trabajador.nombre}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: ErrorServidorException) {
                        // El servidor respondió ERROR (p. ej. contraseña incorrecta)
                        runOnUiThread {
                            btnEntrar.isEnabled = true
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        // Fallo de red: servidor apagado, IP mal puesta…
                        runOnUiThread {
                            btnEntrar.isEnabled = true
                            Toast.makeText(this, "No se puede conectar con el servidor", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()   // sin start() el hilo se crea pero nunca arranca
            }
        }
    }
}