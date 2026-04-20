package com.example.rwai

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppLogger {

    /**
     * Registra un mensaje en el archivo de logs local.
     */
    fun log(context: Context, mensaje: String, esError: Boolean = false) {
        try {
            val archivo = File(context.filesDir, "app_logs.txt")
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val tipo = if (esError) "[ERROR]" else "[INFO]"

            archivo.appendText("$timestamp $tipo $mensaje\n")
        } catch (e: Exception) {
            Log.e("AppLogger", "Error al escribir en el archivo de log", e)
        }
    }

    /**
     * Configura un manejador de excepciones no capturadas para registrar errores críticos.
     */
    fun initCrashHandler(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            val stackTrace = exception.stackTraceToString()

            log(
                context = context,
                mensaje = "Error no controlado en el hilo [${thread.name}]:\n$stackTrace",
                esError = true
            )

            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}