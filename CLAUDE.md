# CLAUDE.md — AI Call Receptionist (Recepcionista de Llamadas con IA)

## Identidad del Proyecto

**Nombre:** AI Call Receptionist  
**Plataforma:** Android (Kotlin)  
**Tipo:** Aplicación VoIP con identificación y enrutamiento inteligente de llamadas  
**Arquitectura:** Cliente Android + Backend PBX (lógica de enrutamiento)  
**Estado actual:** En desarrollo activo  

---

## Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Kotlin (100%) |
| IDE | Android Studio |
| UI | Jetpack Compose |
| Telefonía VoIP | SIP stack (definir: Linphone / PJSIP / WebRTC) |
| IA / NLP | (definir: Whisper / Gemini API / OpenAI) |
| Arquitectura | MVVM + Clean Architecture |
| DI | Hilt |
| Async | Coroutines + Flow |
| Pruebas | JUnit 5, MockK, Espresso |
| Build | Gradle (Kotlin DSL) |
| CI | (definir: GitHub Actions / Bitrise) |

---

## Arquitectura del Sistema

```
[Llamada entrante]
       │
       ▼
[Capa SIP / VoIP]          ← Recibe y gestiona señalización
       │
       ▼
[CallManager Service]      ← Servicio Android en foreground
       │
  ┌────┴────┐
  ▼         ▼
[AI Engine] [PBX Router]   ← Identificación + Enrutamiento
  │
  ▼
[UI / Notificación]        ← Muestra caller ID, contexto, acción sugerida
```

---

## Convenciones de Código

### Nombrado
- **Clases:** `PascalCase` → `CallRoutingService`, `SipSessionManager`
- **Funciones / variables:** `camelCase` → `handleIncomingCall()`, `callerProfile`
- **Constantes:** `SCREAMING_SNAKE_CASE` → `MAX_RETRY_COUNT`
- **Archivos de recursos:** `snake_case` → `activity_main.xml`

### Estructura de Paquetes
```
com.josedmv.aireceptionist/
├── core/               ← Utilidades, extensiones, base classes
├── data/               ← Repository, DataSource, DTOs
├── domain/             ← UseCases, Models, Interfaces
├── presentation/       ← ViewModels, Composables, UI State
├── service/            ← CallManager, SipService (Foreground)
├── ai/                 ← Motor de NLP / Identificación
└── di/                 ← Módulos de Hilt
```

### Reglas de Arquitectura
1. Los `ViewModel` **nunca** acceden directamente a `Repository` — siempre vía `UseCase`
2. Los `UseCase` retornan `Flow<Result<T>>` o `suspend fun` que devuelve `Result<T>`
3. Los `Service` de Android se comunican con la capa de dominio mediante eventos (SharedFlow)
4. Cero lógica de negocio en Composables — solo UI state rendering

---

## Contexto de Dominio: VoIP y PBX

- **SIP (Session Initiation Protocol):** Protocolo de señalización para iniciar/terminar llamadas VoIP
- **PBX (Private Branch Exchange):** Sistema de enrutamiento privado de llamadas
- **Caller ID:** Identificación del número entrante — base para la lógica de IA
- **Enrutamiento:** Decisión de hacia dónde/quién va la llamada según reglas + contexto IA
- **DTMF:** Tonos de marcado — pueden usarse para menús de voz interactivos (IVR)

---

## Tareas en Progreso

- [ ] Integración SIP stack (evaluar Linphone SDK vs PJSIP)
- [ ] Servicio foreground para recibir llamadas en background
- [ ] Pipeline de identificación con IA (transcripción + clasificación)
- [ ] Módulo de reglas de enrutamiento (configurable por el usuario)
- [ ] UI de llamada entrante con info contextual del caller

---

## Comandos de Build y Pruebas

```bash
# Build debug
./gradlew assembleDebug

# Ejecutar unit tests
./gradlew test

# Ejecutar tests de instrumentación
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Build release
./gradlew assembleRelease
```

---

## Permisos Críticos de Android

```xml
<!-- Telefonía -->
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
<uses-permission android:name="android.permission.RECORD_AUDIO"/>

<!-- Red -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

<!-- Foreground Service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL"/>
```

---

## Reglas para Claude

1. **Primero los principios:** Antes de dar una solución, explica el "por qué" desde fundamentos.
2. **Arquitectura primero:** Antes de generar código, confirma que respeta MVVM + Clean Architecture.
3. **Kotlin idiomático:** Usa `sealed class`, `data class`, `object`, extension functions donde aplique. Evita patrones Java.
4. **Manejo de errores explícito:** Toda operación de red/SIP debe manejar errores con `Result<T>` o `sealed class` de estado.
5. **Coroutines, no callbacks:** Si ves callbacks legacy, migra a `suspendCoroutine` o `callbackFlow`.
6. **Tests obligatorios:** Para cada `UseCase` nuevo, genera el test unitario correspondiente.
7. **Sin lógica en XML/Composables:** Los Composables solo reciben `UiState` y emiten eventos.
8. **Contexto de VoIP:** Antes de tocar código de telefonía, revisa siempre el ciclo de vida del SIP dialog.

---

## Desarrollador

**Nombre:** José Demetrio Valencia Mota  
**Perfil:** Full Stack Jr. Developer | Kotlin + Android | VoIP / PBX  
**IDE principal:** Android Studio + VS Code  
**Nivel en Kotlin:** Intermedio — priorizar explicaciones con analogías multidisciplinarias  
