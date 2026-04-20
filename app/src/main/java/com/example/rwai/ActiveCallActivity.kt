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

class ActiveCallActivity : ComponentActivity() {

    // Variable para sostener el control del sensor de proximidad
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onStart() {
        super.onStart()
        // Adquirimos acceso al Sistema Nervioso Autónomo de Energía
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager

        // Verificamos si el teléfono soporta apagar la pantalla por proximidad
        if (powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "RwAI:ProximityLock"
            )
            // Activamos el sensor.
            wakeLock?.acquire()
        }
    }

    override fun onStop() {
        super.onStop()
        // Liberamos el hardware al salir para no gastar batería
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INYECCIÓN DEL PASE VIP ---
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

        // 1. Extraemos los datos del Intent
        val numeroContacto = intent.getStringExtra("NUMERO_CONTACTO") ?: "Número Desconocido"
        val nombreContacto = intent.getStringExtra("NOMBRE_CONTACTO") ?: "Desconocido"

        // 2. Calculamos las iniciales (Ej.: "Juan Delgado" -> "JD")
        val iniciales = nombreContacto.split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2)
            .joinToString("")
            .uppercase()

        setContent {
            PantallaLlamadaActiva(
                numero = numeroContacto,
                iniciales = iniciales,
                nombreContacto = nombreContacto,
                onColgar = {
                    AdministradorDeLlamadas.llamadaActual?.disconnect()
                    finish() // Destruye la sinapsis visual
                }
            )
        }
    }
}
// Asegúrate de tener este import arriba:
// import androidx.compose.ui.platform.LocalContext

@Composable
fun PantallaLlamadaActiva(
    numero: String,
    iniciales: String,
    nombreContacto: String,
    onColgar: () -> Unit
) {
    // --- METABOLISMO: Estados ---
    var segundosLlamada by remember { mutableIntStateOf(0) }
    var altavozActivado by remember { mutableStateOf(false) }
    var micSilenciado by remember { mutableStateOf(false) }
    var mostrarTeclado by remember { mutableStateOf(false) }

    // 1. INYECCIÓN DEL CONTEXTO: Obtenemos una referencia al "Cuerpo" (Activity)
    val context = LocalContext.current

    // Cronómetro Asíncrono
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            segundosLlamada++
        }
    }

    val minutosStr = (segundosLlamada / 60).toString().padStart(2, '0')
    val segundosStr = (segundosLlamada % 60).toString().padStart(2, '0')
    val tiempoFormateado = "$minutosStr:$segundosStr"

    val infiniteTransition = rememberInfiniteTransition(label = "ondas_colgar")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.5f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "ripple_scale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Restart), label = "ripple_alpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {

        // --- CAPA SUPERIOR: TopAppBar ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp).align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, "Segura", tint = Color(0xFF78DC77), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("LLAMADA SEGURA EN CURSO", color = Color(0xFF78DC77), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            Text("HD Voice", color = Color(0xFFBECAB9), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        // --- ZONA DINÁMICA: IDENTIDAD O TECLADO ---
        // Solo un contenedor maestro. Sin columnas anidadas redundantes.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 120.dp, start = 32.dp, end = 32.dp)
                .align(Alignment.TopStart)
        ) {
            if (mostrarTeclado) {
                Text("TECLADO ACTIVO", color = Color(0xFF78DC77), fontSize = 12.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(24.dp))
                val numeros = listOf('1','2','3','4','5','6','7','8','9','*','0','#')
                Column {
                    for (fila in 0..3) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            for (col in 0..2) {
                                val char = numeros[fila * 3 + col]
                                Box(
                                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color(0xFF2A2A2A))
                                        .clickable { AdministradorDeLlamadas.llamadaActual?.playDtmfTone(char) },
                                    contentAlignment = Alignment.Center
                                ) { Text(char.toString(), color = Color.White, fontSize = 28.sp) }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // El contenido de la identidad va directamente aquí, heredando el padding del padre
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
                    Text(iniciales, color = Color(0xFF78DC77), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
                Text(nombreContacto.replace(" ", "\n"), color = Color(0xFFE2E2E2), fontSize = 64.sp, fontWeight = FontWeight.Black, lineHeight = 60.sp, letterSpacing = (-2).sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(numero, color = Color(0xFFBECAB9), fontSize = 18.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF2A2A2A).copy(alpha = 0.5f))
                        .border(1.dp, Color(0xFF3F4A3C).copy(alpha = 0.2f), RoundedCornerShape(50)).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Mic, "Mic", tint = Color(0xFF78DC77), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(tiempoFormateado, color = Color(0xFF78DC77), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- SISTEMA ENDÓCRINO: Panel de Control Quirúrgico ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).align(Alignment.BottomCenter).padding(bottom = 200.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BotonControl(Icons.Outlined.MicOff, "MUTE", micSilenciado) {
                micSilenciado = !micSilenciado
                AdministradorDeLlamadas.servicioActual?.setMuted(micSilenciado)
            }
            BotonControl(Icons.Outlined.Dialpad, "KEYPAD", mostrarTeclado) {
                mostrarTeclado = !mostrarTeclado
            }

            // 2. USO DEL CONTEXTO: Lanzamos la actividad usando el contexto extraído
            BotonControl(Icons.Outlined.PersonAdd, "ADD") {
                val intentDial = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intentDial) // Solucionado el error
            }

            BotonControl(Icons.AutoMirrored.Outlined.VolumeUp, "SPEAKER", altavozActivado) {
                altavozActivado = !altavozActivado
                val rutaAudio = if (altavozActivado) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_WIRED_OR_EARPIECE
                AdministradorDeLlamadas.servicioActual?.setAudioRoute(rutaAudio)
            }
        }

        // --- DESTRUCCIÓN DEL ENLACE: Botón Colgar ---
        Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(110.dp).scale(rippleScale).alpha(rippleAlpha).clip(CircleShape).background(Color(0xFFFFB3AE)))
            Box(
                modifier = Modifier.size(110.dp).clip(CircleShape).background(Color(0xFFFFB3AE)).clickable { onColgar() },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CallEnd, "Colgar", tint = Color(0xFF131313), modifier = Modifier.size(48.dp)) }
        }
    }
}

// NUESTRA CÉLULA PURIFICADA (Ahora acepta clics y cambia de color)
@Composable
fun BotonControl(
    icono: ImageVector,
    etiqueta: String,
    activo: Boolean = false,
    onClick: () -> Unit = {}
) {
    val colorFondo = if (activo) Color(0xFFE2E2E2) else Color(0xFF353535).copy(alpha = 0.4f)
    val colorIcono = if (activo) Color(0xFF131313) else Color(0xFFE2E2E2)
    val colorTexto = if (activo) Color.White else Color(0xFFBECAB9)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colorFondo)
                .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icono, contentDescription = etiqueta, tint = colorIcono, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = etiqueta, color = colorTexto, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
}