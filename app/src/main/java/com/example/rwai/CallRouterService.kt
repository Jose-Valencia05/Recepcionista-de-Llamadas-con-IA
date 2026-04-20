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
 */
class CallRouterService : InCallService() {

    override fun onCreate() {
        super.onCreate()
        // Inicializa el manejador de errores global.
        AppLogger.initCrashHandler(this)
    }

    /**
     * Obtiene la hora actual formateada.
     */
    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    private fun registerAnsweredCall(number: String, name: String, type: Int) {
        val index = CallRecordStore.historyRecords.indexOfFirst { it.number == number }
        if (index != -1) {
            val existing = CallRecordStore.historyRecords[index]
            CallRecordStore.historyRecords[index] = existing.copy(
                frequency = existing.frequency + 1,
                timestamp = getCurrentTime(),
                callType = type
            )
        } else {
            CallRecordStore.historyRecords.add(0, CallRecord(number, name, type, getCurrentTime(), 1))
        }
    }

    private fun registerMissedCall(number: String, name: String, type: Int) {
        val index = CallRecordStore.notificationRecords.indexOfFirst { it.number == number }
        if (index != -1) {
            val existing = CallRecordStore.notificationRecords[index]
            CallRecordStore.notificationRecords[index] = existing.copy(
                frequency = existing.frequency + 1,
                timestamp = getCurrentTime(),
                callType = type
            )
        } else {
            CallRecordStore.notificationRecords.add(0, CallRecord(number, name, type, getCurrentTime(), 1))
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        val incomingNumber = call.details.handle?.schemeSpecificPart ?: "Hidden"
        val currentState = call.details.state

        // Gestión de llamadas entrantes
        if (currentState == Call.STATE_RINGING) {
            val contactName = getContactName(incomingNumber)

            if (contactName != null) {
                Log.d("CallRouter", "Contact recognized: $contactName")

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
                Log.d("CallRouter", "Unknown number detected: $incomingNumber. Rejecting call.")
                call.reject(Call.REJECT_REASON_DECLINED)
                registerMissedCall(incomingNumber, "Unknown", CallLog.Calls.REJECTED_TYPE)
                sendBlockedCallNotification(incomingNumber)
            }
        }
        // Gestión de llamadas salientes
        else if (currentState == Call.STATE_CONNECTING || currentState == Call.STATE_DIALING) {

            Log.d("CallRouter", "Initiating outgoing call to: $incomingNumber")
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
        Log.d("CallRouter", "Call ended and resources released.")
    }

    /**
     * Consulta el nombre del contacto asociado a un número de teléfono.
     */
    private fun getContactName(number: String): String? {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (index != -1) return cursor.getString(index)
            }
        }
        return null
    }

    /**
     * Envía una notificación cuando una llamada es interceptada/bloqueada.
     */
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

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Call Blocked")
            .setContentText("Blocked number: $number")
            .setAutoCancel(true)
            .build()

        notificationManager.notify(number.hashCode(), notification)
    }
}