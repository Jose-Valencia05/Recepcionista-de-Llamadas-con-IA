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

class IncomingCallActivity : ComponentActivity() {

    // Variable para sostener el control del sensor
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStart() {
        super.onStart()
        // Adquirimos acceso al Sistema Nervioso Autónomo de Energía
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Verificamos si el teléfono tiene el hardware físico (Sensor de proximidad)
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "RwAI:ProximityLock"
            )
            // Activamos el sensor. Desde este momento, si algo tapa el sensor, la pantalla muere.
            wakeLock?.acquire()
        }
    }

    override fun onStop() {
        super.onStop()
        // Cuando la llamada termina o la app se oculta, liberamos el hardware
        // para no drenar la batería en segundo plano.
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. INYECCIÓN DEL PASE VIP (Bypass de Pantalla de Bloqueo)
        // Le dice al hardware: "Enciende los LED de la pantalla inmediatamente"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // Soporte para teléfonos más antiguos (Legacy)
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        // 2. Extraemos los datos del Intent (Tu código actual)
        val numeroContacto = intent.getStringExtra("NUMERO_CONTACTO") ?: "Número Desconocido"
        val nombreContacto = intent.getStringExtra("NOMBRE_CONTACTO") ?: "Contacto"

        // Algoritmo para extraer las iniciales (Ej.: "Carlos Pérez" -> "CP")
        val iniciales = nombreContacto.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        setContent {
            PantallaLlamadaFigma(
                numero = numeroContacto,
                iniciales = iniciales,
                nombreContacto = nombreContacto,
                onContestar = {
                    AdministradorDeLlamadas.llamadaActual?.answer(VideoProfile.STATE_AUDIO_ONLY)

                    // Inyectamos el impulso hacia la nueva pantalla
                    val intent = Intent(this@IncomingCallActivity, ActiveCallActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("NUMERO_CONTACTO", numeroContacto)
                        putExtra("NOMBRE_CONTACTO", nombreContacto)
                    }
                    startActivity(intent)

                    finish() // Ahora sí, matamos la pantalla de entrada
                },
                onRechazar = {
                    AdministradorDeLlamadas.llamadaActual?.reject(Call.REJECT_REASON_DECLINED)
                    finish()
                }
            )
        }
    }
}

@Composable
fun PantallaLlamadaFigma(
    numero: String,
    iniciales: String,
    nombreContacto: String,
    onContestar: () -> Unit,
    onRechazar: () -> Unit
) {
    // --- 1. CONFIGURACIÓN DE ANIMACIONES CINESTÉSICAS ---
    val infiniteTransition = rememberInfiniteTransition(label = "breathing_pulse")

    // Pulso Primario (Contestar - Verde) -> Pulso de Vida
    val scalePrimary by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f, // Crece ligeramente
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePrimary"
    )

    // --- 2. ESTRUCTURA BASE (Fondo Negro Absoluto OLED) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)) // Pure Black de Figma
            .padding(horizontal = 24.dp)
    ) {
        // --- 3. CAPA SUPERIOR: TopAppBar (Alineación Horizontal) ---
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
                    imageVector = Icons.Default.Security, // Shield icon
                    contentDescription = "Shield",
                    tint = Color(0xFF78DC77), // Green primary
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "INCOMING CALL",
                    color = Color(0xFFE2E2E2), // Text primary
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

        // --- 4. CUERPO PRINCIPAL: Info del Contacto (Alineación Editorial Izquierda) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .align(Alignment.TopStart)
        ) {
            // Avatar Circular (Gris Oscuro con iniciales Verdes)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A2A)), // dark gray surface
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    color = Color(0xFF78DC77), // Green
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Nombre (Grande, Negrita, Blanco)
            Text(
                text = nombreContacto,
                color = Color(0xFFE2E2E2), // White
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 56.sp,
                letterSpacing = (-1.5).sp // Kerning negativo para look editorial
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Número (Gris Claro)
            Text(
                text = numero,
                color = Color(0xFFBECAB9), // Text variant
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Indicador de estado (Punto Verde parpadeante)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF78DC77))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "LLAMADA ENTRANTE DE CONTACTO...",
                    color = Color(0xFFBECAB9).copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // --- 5. ACCIONES RÁPIDAS (Panel flotante translúcido) ---
        // Se alinea abajo a la izquierda, pero con padding alto para flotar sobre los botones
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 160.dp) // Flota sobre los botones de abajo
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B1B1B).copy(alpha = 0.8f)) // Simulación Glassmorphism
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
                        // Icono Mensaje (Outline)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* Intent SMS */ }
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Msg", tint = Color(0xFFE2E2E2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Message", color = Color(0xFFE2E2E2), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        // Icono Recordatorio (Outline)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { /* Intent Calendario */ }
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = "Remind", tint = Color(0xFFE2E2E2), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Remind", color = Color(0xFFE2E2E2), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // --- 6. BOTONES DE ACCIÓN PRINCIPALES (Bottom Center) ---
        // --- 6. SEGURO MECÁNICO: Deslizador de Acción ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            // Variables de fricción y movimiento
            var offsetX by remember { mutableFloatStateOf(0f) }
            val limiteDeslizamiento = 250f // Píxeles máximos que se puede mover
            val umbralActivacion = 150f // Píxeles necesarios para activar la acción

            // Pista/Carril visual de fondo
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                // Indicadores visuales en los extremos
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color(0xFFFFB3AE), modifier = Modifier.size(28.dp))
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF78DC77), modifier = Modifier.size(28.dp))
                }
            }

            // El Botón Central Deslizable (Thumb)
            Box(
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), 0) } // Translación vectorial
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2E2E2))
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                // Evaluamos la posición final cuando el usuario suelta el dedo
                                if (offsetX > umbralActivacion) {
                                    onContestar()
                                } else if (offsetX < -umbralActivacion) {
                                    onRechazar()
                                } else {
                                    // Si no cruzó el umbral, el botón regresa al centro (resorte)
                                    offsetX = 0f
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                // Actualizamos la posición, pero no dejamos que salga de la pista
                                offsetX = (offsetX + dragAmount).coerceIn(-limiteDeslizamiento, limiteDeslizamiento)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Icono central (Escudo dinámico)
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Desliza",
                    tint = Color(0xFF131313)
                )
            }
        }
    }
}