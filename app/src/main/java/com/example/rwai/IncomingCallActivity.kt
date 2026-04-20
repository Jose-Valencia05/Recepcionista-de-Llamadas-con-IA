package com.example.rwai

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.telecom.VideoProfile
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Actividad que gestiona la interfaz de usuario para llamadas entrantes.
 */
class IncomingCallActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStart() {
        super.onStart()
        // Gestión del sensor de proximidad.
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "RwAI:ProximityLock"
            )
            wakeLock?.acquire()
        }
    }

    override fun onStop() {
        super.onStop()
        // Liberación del sensor al detener la actividad.
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración para mostrar la actividad sobre la pantalla de bloqueo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val phoneNumber = intent.getStringExtra("NUMERO_CONTACTO") ?: "Unknown Number"
        val contactName = intent.getStringExtra("NOMBRE_CONTACTO") ?: "Contact"

        val initials = contactName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        setContent {
            IncomingCallScreen(
                number = phoneNumber,
                initials = initials,
                contactName = contactName,
                onAnswer = {
                    CallManager.currentCall?.answer(VideoProfile.STATE_AUDIO_ONLY)

                    val intent = Intent(this@IncomingCallActivity, ActiveCallActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("NUMERO_CONTACTO", phoneNumber)
                        putExtra("NOMBRE_CONTACTO", contactName)
                    }
                    startActivity(intent)
                    finish()
                },
                onReject = {
                    CallManager.currentCall?.reject(Call.REJECT_REASON_DECLINED)
                    finish()
                }
            )
        }
    }
}

@Composable
fun IncomingCallScreen(
    number: String,
    initials: String,
    contactName: String,
    onAnswer: () -> Unit,
    onReject: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")

    val scalePrimary by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePrimary"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(horizontal = 24.dp)
    ) {
        // Barra superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Secure",
                    tint = Color(0xFF78DC77),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INCOMING CALL",
                    color = Color(0xFFE2E2E2),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "HD Voice",
                color = Color(0xFFE2E2E2),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Información del contacto
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .align(Alignment.TopStart)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color(0xFF78DC77),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = contactName,
                color = Color(0xFFE2E2E2),
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 56.sp,
                letterSpacing = (-1.5).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = number,
                color = Color(0xFFBECAB9),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF78DC77))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "INCOMING CALL FROM CONTACT...",
                    color = Color(0xFFBECAB9).copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Acciones rápidas
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 160.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B1B1B).copy(alpha = 0.8f))
                    .border(1.dp, Color(0xFFE2E2E2).copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = "QUICK ACTIONS",
                        color = Color(0xFFBECAB9).copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* SMS Intent */ }
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Msg", tint = Color(0xFFE2E2E2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message", color = Color(0xFFE2E2E2), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* Calendar Intent */ }
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = "Remind", tint = Color(0xFFE2E2E2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remind", color = Color(0xFFE2E2E2), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Deslizador para contestar o rechazar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            var offsetX by remember { mutableFloatStateOf(0f) }
            val dragLimit = 250f
            val activationThreshold = 150f

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color(0xFFFFB3AE), modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF78DC77), modifier = Modifier.size(28.dp))
                }
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E2E2))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > activationThreshold) {
                                    onAnswer()
                                } else if (offsetX < -activationThreshold) {
                                    onReject()
                                } else {
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount).coerceIn(-dragLimit, dragLimit)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Slide",
                    tint = Color(0xFF131313)
                )
            }
        }
    }
}