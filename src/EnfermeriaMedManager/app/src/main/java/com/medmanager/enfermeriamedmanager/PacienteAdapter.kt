package com.medmanager.enfermeriamedmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.medmanager.comun.PacienteDTO

/**
 * Pinta la lista de resultados del buscador (una tarjeta por paciente, layout item_paciente).
 * @param alPulsar qué hacer cuando la enfermera toca un paciente (abrir su ficha rápida).
 */
class PacienteAdapter(
    private val alPulsar: (PacienteDTO) -> Unit
) : RecyclerView.Adapter<PacienteAdapter.Fila>() {

    private var pacientes: List<PacienteDTO> = emptyList()

    /** Sustituye la lista mostrada por los nuevos resultados. */
    fun mostrar(nuevos: List<PacienteDTO>) {
        pacientes = nuevos
        notifyDataSetChanged()   // lista pequeña (máx. 50): basta con repintarla entera
    }

    /** Guarda las vistas de una fila para no buscarlas con findViewById cada vez que se reutiliza. */
    class Fila(vista: View) : RecyclerView.ViewHolder(vista) {
        val tvNombre: TextView = vista.findViewById(R.id.tvNombre)
        val tvDatos: TextView = vista.findViewById(R.id.tvDatos)
        val tvAlergias: TextView = vista.findViewById(R.id.tvAlergias)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Fila {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.item_paciente, parent, false)
        return Fila(vista)
    }

    override fun getItemCount() = pacientes.size

    override fun onBindViewHolder(fila: Fila, posicion: Int) {
        val p = pacientes[posicion]
        val ctx = fila.itemView.context

        fila.tvNombre.text = "${p.apellidos}, ${p.nombre}"

        val nacimiento = p.fechaNacimiento
        fila.tvDatos.text = if (nacimiento != null) {
            ctx.getString(R.string.datos_paciente, p.dni, edad(nacimiento))
        } else {
            ctx.getString(R.string.datos_paciente_sin_edad, p.dni)
        }

        // Las alergias se resaltan en rojo: es lo primero que debe ver enfermería
        val alergias = p.alergias?.trim().orEmpty()
        if (alergias.isNotEmpty()) {
            fila.tvAlergias.text = ctx.getString(R.string.alergias, alergias)
            fila.tvAlergias.setTextColor(ContextCompat.getColor(ctx, R.color.alerta))
        } else {
            fila.tvAlergias.text = ctx.getString(R.string.sin_alergias)
            fila.tvAlergias.setTextColor(fila.tvDatos.currentTextColor)
        }

        fila.itemView.setOnClickListener { alPulsar(p) }
    }
}
