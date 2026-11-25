package com.medmanager.enfermeriamedmanager.red

import com.medmanager.comun.Acciones
import com.medmanager.comun.CitaDTO
import com.medmanager.comun.LoginDTO
import com.medmanager.comun.PacienteDTO
import com.medmanager.comun.PaqueteRed
import com.medmanager.comun.TrabajadorDTO
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetSocketAddress
import java.net.Socket

/** Error de negocio que devuelve el servidor (p. ej. contraseña incorrecta). */
class ErrorServidorException(mensaje: String) : Exception(mensaje)

/**
 * Una única conexión con el servidor para toda la app (object = singleton en Kotlin).
 * El servidor guarda la sesión por conexión, así que no se debe abrir una nueva en cada petición.
 */
object ClienteSocket {
    private const val HOST = "10.0.2.2"   // en el emulador, 10.0.2.2 es el "localhost" del PC
    private const val PUERTO = 8080

    private var socket: Socket? = null
    private var salida: ObjectOutputStream? = null
    private var entrada: ObjectInputStream? = null

    var usuarioActual: TrabajadorDTO? = null
        private set

    /** Envía una petición y devuelve el payload de la respuesta OK. */
    @Synchronized
    fun pedir(peticion: PaqueteRed): Any? {
        try {
            conectar()
            salida!!.writeObject(peticion)
            salida!!.flush()
            val respuesta = entrada!!.readObject() as PaqueteRed
            if (respuesta.accion == Acciones.ERROR) {
                throw ErrorServidorException(respuesta.payload.toString())
            }
            return respuesta.payload
        } catch (e: IOException) {
            cerrar()   // la conexión ya no es fiable
            throw e
        }
    }

    /** Inicia sesión y recuerda quién es el usuario. */
    fun login(usuario: String, password: String): TrabajadorDTO {
        val trabajador = pedir(PaqueteRed(Acciones.LOGIN, LoginDTO(usuario, password))) as TrabajadorDTO
        usuarioActual = trabajador
        return trabajador
    }

    /**
     * Busca pacientes activos por DNI, nombre o apellidos (máx. 50, los limita el servidor).
     * Para enfermería el servidor incluye las alergias (RN3).
     * Bloquea el hilo: llamar desde Dispatchers.IO.
     */
    fun buscarPacientes(texto: String): List<PacienteDTO> {
        val resultado = pedir(PaqueteRed(Acciones.BUSCAR_PACIENTES, texto)) as List<*>
        return resultado.filterIsInstance<PacienteDTO>()
    }

    /**
     * Citas de hoy de un paciente (de cualquier médico y estado), ordenadas por hora.
     * Bloquea el hilo: llamar desde Dispatchers.IO.
     */
    fun citasPacienteHoy(idPaciente: Int): List<CitaDTO> {
        val resultado = pedir(PaqueteRed(Acciones.CITAS_PACIENTE_HOY, idPaciente)) as List<*>
        return resultado.filterIsInstance<CitaDTO>()
    }

    /**
     * Guarda las observaciones de enfermería de una cita (pendiente y de hoy; lo comprueba el servidor).
     * Un texto vacío las borra. Devuelve la cita ya actualizada.
     * Bloquea el hilo: llamar desde Dispatchers.IO.
     */
    fun guardarObservaciones(idCita: Int, observaciones: String): CitaDTO =
        pedir(PaqueteRed(Acciones.GUARDAR_OBSERVACIONES, CitaDTO.conObservaciones(idCita, observaciones))) as CitaDTO

    private fun conectar() {
        if (socket?.isClosed == false) return   // ya conectados
        val nuevo = Socket()
        nuevo.connect(InetSocketAddress(HOST, PUERTO), 3000)
        nuevo.soTimeout = 10000
        socket = nuevo
        // Primero la salida (y flush), después la entrada: si no, se bloquea
        salida = ObjectOutputStream(nuevo.getOutputStream())
        salida!!.flush()
        entrada = ObjectInputStream(nuevo.getInputStream())
    }

    @Synchronized
    fun cerrar() {
        usuarioActual = null
        try { socket?.close() } catch (_: IOException) { }
        socket = null
        salida = null
        entrada = null
    }
}