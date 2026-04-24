package com.example.rwai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Servicio encargado de gestionar las llamadas entrantes y salientes.
 * Implementa lógica de filtrado silencioso para evitar interrupciones innecesarias.
 */
class CallRouterService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        AppLogger.initCrashHandler(this)
        AppLogger.log(this, "CallRouterService creado.")
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun registerAnsweredCall(number: String, name: String, type: Int) {
        val index = CallRecordStore.historyRecords.indexOfFirst { it.number == number }
        val (country, location) = NumberIdentifier.identify(number)
        
        if (index != -1) {
            val existing = CallRecordStore.historyRecords[index]
            CallRecordStore.historyRecords[index] = existing.copy(
                frequency = existing.frequency + 1,
                timestamp = getCurrentTime(),
                callType = type,
                country = country,
                location = location
            )
        } else {
            CallRecordStore.historyRecords.add(0, CallRecord(
                number = number, 
                name = name, 
                callType = type, 
                timestamp = getCurrentTime(), 
                frequency = 1, 
                country = country, 
                location = location,
                epochTime = System.currentTimeMillis()
            ))
        }
    }

    private fun registerMissedCall(number: String, name: String, type: Int) {
        val index = CallRecordStore.notificationRecords.indexOfFirst { it.number == number }
        val (country, location) = NumberIdentifier.identify(number)

        if (index != -1) {
            val existing = CallRecordStore.notificationRecords[index]
            CallRecordStore.notificationRecords[index] = existing.copy(
                frequency = existing.frequency + 1,
                timestamp = getCurrentTime(),
                callType = type,
                country = country,
                location = location
            )
        } else {
            CallRecordStore.notificationRecords.add(0, CallRecord(
                number = number, 
                name = name, 
                callType = type, 
                timestamp = getCurrentTime(), 
                frequency = 1, 
                country = country, 
                location = location,
                epochTime = System.currentTimeMillis()
            ))
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val incomingNumber = call.details.handle?.schemeSpecificPart ?: "Hidden"
        val currentState = call.details.state
        
        AppLogger.log(this, "Nueva llamada detectada: $incomingNumber (Estado: $currentState)")

        if (currentState == Call.STATE_RINGING) {
            val contactName = getContactName(incomingNumber)

            if (contactName != null) {
                AppLogger.log(this, "Contacto reconocido: $contactName. Permitiendo interrupción.")

                val callCallback = object : Call.Callback() {
                    var wasAnswered = false
                    override fun onStateChanged(call: Call, state: Int) {
                        if (state == Call.STATE_ACTIVE && !wasAnswered) {
                            wasAnswered = true
                            registerAnsweredCall(incomingNumber, contactName, CallLog.Calls.INCOMING_TYPE)
                        } else if (state == Call.STATE_DISCONNECTED && !wasAnswered) {
                            registerMissedCall(incomingNumber, contactName, CallLog.Calls.MISSED_TYPE)
                        }
                    }
                }
                call.registerCallback(callCallback)

                CallManager.currentCall = call
                CallManager.currentService = this

                val intent = Intent(this, IncomingCallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("NUMERO_CONTACTO", incomingNumber)
                    putExtra("NOMBRE_CONTACTO", contactName)
                }
                startActivity(intent)

            } else {
                // LÓGICA DE FILTRADO SILENCIOSO: 
                // Al rechazar la llamada inmediatamente aquí, evitamos que el sistema
                // notifique al usuario o interrumpa dispositivos Bluetooth.
                AppLogger.log(this, "Número desconocido: $incomingNumber. Rechazando llamada silenciosamente.")
                
                try {
                    call.reject(Call.REJECT_REASON_DECLINED)
                } catch (e: Exception) {
                    AppLogger.log(this, "Error al rechazar llamada: ${e.message}", true)
                }
                
                registerMissedCall(incomingNumber, "Unknown", CallLog.Calls.REJECTED_TYPE)
                sendBlockedCallNotification(incomingNumber)
            }
        }
        else if (currentState == Call.STATE_CONNECTING || currentState == Call.STATE_DIALING) {
            val contactName = getContactName(incomingNumber) ?: "Unknown"

            val callCallback = object : Call.Callback() {
                var wasAnswered = false
                override fun onStateChanged(call: Call, state: Int) {
                    if (state == Call.STATE_ACTIVE && !wasAnswered) {
                        wasAnswered = true
                        registerAnsweredCall(incomingNumber, contactName, CallLog.Calls.OUTGOING_TYPE)
                    }
                }
            }
            call.registerCallback(callCallback)

            CallManager.currentCall = call
            CallManager.currentService = this

            val intent = Intent(this, ActiveCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NUMERO_CONTACTO", incomingNumber)
                putExtra("NOMBRE_CONTACTO", contactName)
            }
            startActivity(intent)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        AppLogger.log(this, "Llamada finalizada.")
    }

    private fun getContactName(number: String): String? {
        return try {
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (index != -1) return cursor.getString(index)
                }
            }
            null
        } catch (e: Exception) {
            AppLogger.log(this, "Error al buscar contacto: ${e.message}", true)
            null
        }
    }

    private fun sendBlockedCallNotification(number: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "blocked_calls_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Blocked Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val (country, location) = NumberIdentifier.identify(number)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Call Blocked")
            .setContentText("Blocked number: $number ($location, $country)")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(number.hashCode(), notification)
    }
}