package com.example.rwai

import android.telecom.Call
import android.telecom.InCallService

object AdministradorDeLlamadas {
    // Maneja la red (Contestar/Colgar)
    var llamadaActual: Call? = null

    // NUEVO: Maneja el Hardware (Audio/Micrófono)
    var servicioActual: InCallService? = null
}