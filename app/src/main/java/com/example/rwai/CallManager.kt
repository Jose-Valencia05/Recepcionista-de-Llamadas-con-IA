package com.example.rwai

import android.telecom.Call
import android.telecom.InCallService

/**
 * Singleton encargado de gestionar el estado de la llamada activa y el servicio de llamadas.
 */
object CallManager {
    // Referencia a la llamada actual en curso.
    var currentCall: Call? = null

    // Referencia al servicio de llamadas para gestionar audio y otros estados.
    var currentService: InCallService? = null
}