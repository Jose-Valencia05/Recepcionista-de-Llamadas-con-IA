package com.example.rwai

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CajaNegra {

    // Función de escritura manual (Para tus logs diarios)
    fun escribir(context: Context, mensaje: String, esError: Boolean = false) {
        try {
            val archivo = File(context.filesDir, "rwai_logs.txt")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val tipo = if (esError) "[ERROR FATAL]" else "[INFO]"

            archivo.appendText("$timestamp $tipo $mensaje\n")
        } catch (e: Exception) {
            Log.e("CajaNegra", "Fallo crítico al escribir en bitácora", e)
        }
    }

    // EL INTERCEPTOR GLOBAL (La Red de Seguridad)
    fun instalarRedDeSeguridad(context: Context) {
        // Guardamos la guillotina por defecto del sistema operativo
        val manejadorPorDefecto = Thread.getDefaultUncaughtExceptionHandler()

        // Instalamos nuestro propio interceptor en el hilo maestro
        Thread.setDefaultUncaughtExceptionHandler { hilo, excepcion ->

            // 1. Extraemos el ADN del error (toda la ruta de la falla)
            val trazaForense = excepcion.stackTraceToString()

            // 2. Lo escribimos en la piedra (disco duro)
            escribir(
                context = context,
                mensaje = "CRASHEO INESPERADO en el hilo [${hilo.name}]:\n$trazaForense",
                esError = true
            )

            // 3. Dejamos caer la guillotina (Permitimos que el OS cierre la app correctamente)
            // Si no hacemos esto, la app se queda como un proceso "Zombi" congelado en la RAM.
            manejadorPorDefecto?.uncaughtException(hilo, excepcion)
        }
    }
}