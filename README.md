# RwAI - Secure Dialer & Call Manager

RwAI es una aplicación de marcador telefónico (Dialer) y gestión de llamadas para Android, enfocada en la seguridad y una interfaz de usuario moderna y minimalista construida con Jetpack Compose.

## Características Principales

- **Marcador Personalizado:** Interfaz de teclado numérico de alta fidelidad.
- **Gestión de Llamadas Activas:** Pantalla de llamada en curso con controles de audio (silenciar, altavoz) y teclado DTMF.
- **Interfaz de Llamada Entrante:** Pantalla personalizada para recibir llamadas con sistema de deslizamiento para contestar o rechazar.
- **Historial de Llamadas Inteligente:** Clasificación automática de llamadas en "Historial" y "Actividad Reciente" (llamadas perdidas/bloqueadas).
- **Seguridad:** Bloqueo automático de números desconocidos y notificaciones de llamadas interceptadas.
- **Logger del Sistema:** Registro detallado de eventos y errores críticos para depuración.
- **Optimización de Hardware:** Uso del sensor de proximidad para gestionar el apagado de pantalla durante las llamadas.

##  Tecnologías Utilizadas

- **Lenguaje:** Kotlin
- **UI Framework:** Jetpack Compose
- **Arquitectura:** Singleton Pattern para gestión de estado global.
- **Android APIs:**
    - `TelecomManager` & `InCallService` para el control de llamadas.
    - `RoleManager` para solicitar ser la aplicación de teléfono predeterminada.
    - `CallLog` & `ContactsContract` para acceso al historial y contactos.
    - `PowerManager` (WakeLocks) para control del hardware.

## Estructura del Proyecto

- `MainActivity.kt`: Punto de entrada, buscador de contactos y acceso al historial.
- `CallRouterService.kt`: Implementación de `InCallService` para interceptar y enrutar llamadas.
- `IncomingCallActivity.kt`: Interfaz de usuario para llamadas entrantes.
- `ActiveCallActivity.kt`: Interfaz de usuario para llamadas en curso.
- `CallManager.kt`: Singleton para gestionar el estado de la llamada activa.
- `CallRecordStore.kt`: Gestión en memoria de los registros de llamadas.
- `AppLogger.kt`: Utilidad de registro de logs y manejo de errores globales.

##  Requisitos e Instalación

1. Clona el repositorio.
2. Abre el proyecto en **Android Studio (Hedgehog o superior)**.
3. Compila y ejecuta en un dispositivo con **Android 10 (API 29)** o superior.
4. Al abrir la app, pulsa en **"Set Default Dialer"** para otorgar los permisos necesarios de gestión de llamadas.

## Permisos Requeridos

La aplicación solicita los siguientes permisos críticos:
- `READ_CALL_LOG`: Para mostrar el historial de llamadas.
- `READ_CONTACTS`: Para identificar llamadas entrantes.
- `CALL_PHONE`: Para iniciar llamadas desde el marcador.
- `MANAGE_OWN_CALLS`: Para la integración con el sistema de telecomunicaciones.

---
*Desarrollado como una solución robusta y privada para la gestión de comunicaciones en Android.*