---
name: voip-debug
description: >
  Diagnostica y depura problemas de VoIP, SIP, audio, o conectividad
  en el proyecto de recepcionista de llamadas. Úsala cuando el usuario
  mencione: llamada que no conecta, audio cortado, timeout de SIP,
  problemas de REGISTER/INVITE/BYE, o errores de red en llamadas.
---

# Skill: Debug VoIP / SIP en Android

## Marco de Diagnóstico

El protocolo SIP funciona como un **protocolo postal de señalización**: primero se intercambian "cartas" (mensajes SIP) para acordar los términos de la llamada, y luego el audio viaja por un canal separado (RTP). Un problema puede estar en la señalización (SIP) o en el medio (RTP/audio) — son capas distintas.

## Paso 1: Identificar la Capa del Problema

Pregunta o detecta automáticamente:

| Síntoma | Capa probable |
|---------|--------------|
| La llamada no inicia / no timbra | SIP Signaling |
| Timbra pero no conecta (no hay audio) | RTP / NAT |
| Audio cortado o con delay | Codec / Buffer |
| Se cae después de X segundos | SIP keepalive / timeout |
| Error de registro (`REGISTER` fails) | Autenticación / DNS |

## Paso 2: Checklist por Capa

### SIP Signaling
- [ ] ¿El SIP server URI es correcto? (IP o dominio + puerto)
- [ ] ¿Las credenciales (usuario/contraseña) están bien configuradas?
- [ ] ¿El `REGISTER` retorna `200 OK`?
- [ ] ¿Hay firewall bloqueando el puerto SIP (default: 5060 UDP/TCP)?
- [ ] ¿El `User-Agent` header es aceptado por el servidor?

### RTP / Audio
- [ ] ¿El dispositivo tiene permiso `RECORD_AUDIO` en runtime?
- [ ] ¿Los codecs negociados (en SDP) son compatibles con el servidor? (G.711, G.722, Opus)
- [ ] ¿NAT traversal está configurado? (STUN/TURN server)
- [ ] ¿El puerto RTP (default: 10000-20000 UDP) está abierto?

### Android Específico
- [ ] ¿El `AudioManager` está en modo `MODE_IN_COMMUNICATION`?
- [ ] ¿El `AudioFocus` está solicitado correctamente antes de la llamada?
- [ ] ¿El Foreground Service está activo antes de la llamada? (Android 14+ lo requiere)
- [ ] ¿`FOREGROUND_SERVICE_PHONE_CALL` está declarado en el Manifest?

## Paso 3: Logging Útil

Genera este bloque de logging si no existe:

```kotlin
object SipLogger {
    private const val TAG = "SIP_DEBUG"
    
    fun logMessage(direction: String, message: String) {
        Log.d(TAG, "[$direction] ${message.take(500)}")
    }
    
    fun logError(context: String, error: Throwable) {
        Log.e(TAG, "[$context] ${error.message}", error)
    }
    
    fun logState(state: String) {
        Log.i(TAG, "[STATE] $state @ ${System.currentTimeMillis()}")
    }
}
```

## Paso 4: Formato de Diagnóstico Final

```
📡 CAPA AFECTADA: [Signaling / RTP / Android / Desconocido]
🔍 CAUSA PROBABLE: [descripción]
🛠️ PASOS DE SOLUCIÓN:
  1. [paso concreto]
  2. [paso concreto]
🧪 CÓMO VERIFICAR: [qué output/log confirma que está resuelto]
```
