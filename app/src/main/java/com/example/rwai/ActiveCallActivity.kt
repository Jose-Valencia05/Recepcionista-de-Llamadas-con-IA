package com.example.rwai

import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.CallAudioState
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Actividad que gestiona la interfaz de usuario durante una llamada activa.
 */
class ActiveCallActivity : ComponentActivity() {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStart() {
        super.onStart()
        // Gestión del sensor de proximidad para apagar la pantalla durante la llamada.
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
        // Liberación del wake lock al detener la actividad.
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuración para mostrar la actividad sobre la pantalla de bloqueo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        val phoneNumber = intent.getStringExtra("NUMERO_CONTACTO") ?: "Unknown Number"
        val contactName = intent.getStringExtra("NOMBRE_CONTACTO") ?: "Unknown"

        val initials = contactName.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        setContent {
            ActiveCallScreen(
                number = phoneNumber,
                initials = initials,
                contactName = contactName,
                onHangUp = {
                    CallManager.currentCall?.disconnect()
                    finish()
                }
            )
        }
    }
}

@Composable
fun ActiveCallScreen(
    number: String,
    initials: String,
    contactName: String,
    onHangUp: () -> Unit
) {
    // Estados de la interfaz de usuario
    var callSeconds by remember { mutableIntStateOf(0) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var isDialpadVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Cronómetro de la llamada
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            callSeconds++
        }
    }

    val minutesStr = (callSeconds / 60).toString().padStart(2, '0')
    val secondsStr = (callSeconds % 60).toString().padStart(2, '0')
    val formattedTime = "$minutesStr:$secondsStr"

    val infiniteTransition = rememberInfiniteTransition(label = "hangup_ripple")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "ripple_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "ripple_alpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {

        // Barra superior de estado
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, "Secure", tint = Color(0xFF78DC77), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SECURE CALL IN PROGRESS", color = Color(0xFF78DC77), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text("HD Voice", color = Color(0xFFBECAB9), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        // Información del contacto o teclado numérico
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp, start = 32.dp, end = 32.dp)
                .align(Alignment.TopStart)
        ) {
            if (isDialpadVisible) {
                Text("DIALPAD ACTIVE", color = Color(0xFF78DC77), fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(24.dp))
                val digits = listOf('1','2','3','4','5','6','7','8','9','*','0','#')
                Column {
                    for (row in 0..3) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (col in 0..2) {
                                val digit = digits[row * 3 + col]
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF2A2A2A))
                                        .clickable { CallManager.currentCall?.playDtmfTone(digit) },
                                    contentAlignment = Alignment.Center
                                ) { Text(digit.toString(), color = Color.White, fontSize = 28.sp) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
                    Text(initials, color = Color(0xFF78DC77), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(contactName.replace(" ", "\n"), color = Color(0xFFE2E2E2), fontSize = 64.sp, fontWeight = FontWeight.Black, lineHeight = 60.sp, letterSpacing = (-2).sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(number, color = Color(0xFFBECAB9), fontSize = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF2A2A2A).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF3F4A3C).copy(alpha = 0.2f), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, "Mic", tint = Color(0xFF78DC77), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(formattedTime, color = Color(0xFF78DC77), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Panel de controles de llamada
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).align(Alignment.BottomCenter).padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ControlButton(Icons.Outlined.MicOff, "MUTE", isMuted) {
                isMuted = !isMuted
                CallManager.currentService?.setMuted(isMuted)
            }
            ControlButton(Icons.Outlined.Dialpad, "KEYPAD", isDialpadVisible) {
                isDialpadVisible = !isDialpadVisible
            }

            ControlButton(Icons.Outlined.PersonAdd, "ADD") {
                val dialIntent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }

            ControlButton(Icons.AutoMirrored.Outlined.VolumeUp, "SPEAKER", isSpeakerOn) {
                isSpeakerOn = !isSpeakerOn
                val audioRoute = if (isSpeakerOn) android.telecom.CallAudioState.ROUTE_SPEAKER else android.telecom.CallAudioState.ROUTE_WIRED_OR_EARPIECE
                CallManager.currentService?.setAudioRoute(audioRoute)
            }
        }

        // Botón para finalizar la llamada
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(110.dp).scale(rippleScale).alpha(rippleAlpha).clip(CircleShape).background(Color(0xFFFFB3AE)))
            Box(
                modifier = Modifier.size(110.dp).clip(CircleShape).background(Color(0xFFFFB3AE)).clickable { onHangUp() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CallEnd, "Hang up", tint = Color(0xFF131313), modifier = Modifier.size(48.dp)) }
        }
    }
}

@Composable
fun ControlButton(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isActive) Color(0xFFE2E2E2) else Color(0xFF353535).copy(alpha = 0.4f)
    val iconColor = if (isActive) Color(0xFF131313) else Color(0xFFE2E2E2)
    val textColor = if (isActive) Color.White else Color(0xFFBECAB9)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}