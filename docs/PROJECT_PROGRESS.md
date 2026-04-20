# 📋 PROJECT_PROGRESS.md
# AI Call Receptionist — Recepcionista de Llamadas con IA

> ⚙️ **Documento Vivo:** Este archivo es actualizado automáticamente por Claude Code
> cada vez que se realiza una modificación al proyecto. No editar manualmente.
> Última actualización: _[Claude actualiza este campo en cada sync]_

---

## 1. Identidad del Proyecto

| Campo | Valor |
|-------|-------|
| **Nombre** | AI Call Receptionist |
| **Plataforma** | Android (Kotlin) |
| **Versión** | 0.1.0-alpha |
| **Estado** | 🟡 En desarrollo — Setup inicial |
| **Repositorio** | _[agregar URL]_ |
| **Desarrollador** | José Demetrio Valencia Mota |
| **Inicio** | _[Claude registra la fecha del primer commit]_ |

---

## 2. Objetivo del Sistema

Sistema Android que actúa como recepcionista inteligente de llamadas VoIP. Identifica al llamante, clasifica la intención de la llamada mediante IA, y toma decisiones de enrutamiento automáticas sin intervención humana.

```
[Llamada entrante SIP]
        │
        ▼
[Identificación del Caller]  ──→  Base de datos de contactos / CRM
        │
        ▼
[Análisis IA de Intención]   ──→  Transcripción + Clasificación NLP
        │
        ▼
[Motor de Enrutamiento PBX]  ──→  Reglas configurables por el usuario
        │
        ▼
[Acción: Transferir / Grabar / Responder automáticamente]
```

---

## 3. Progreso General

```
Setup del Proyecto          [██████████] 100%
Arquitectura Base           [░░░░░░░░░░]   0%
Integración SIP Stack       [░░░░░░░░░░]   0%
Motor de Identificación IA  [░░░░░░░░░░]   0%
Motor de Enrutamiento PBX   [░░░░░░░░░░]   0%
UI de Llamada Entrante      [░░░░░░░░░░]   0%
Tests                       [░░░░░░░░░░]   0%
```

---

## 4. Módulos del Sistema

| Módulo | Descripción | Estado |
|--------|-------------|--------|
| `SipStack` | Gestión de sesiones SIP (señalización) | ⬜ Pendiente |
| `CallManager` | Servicio foreground que orquesta llamadas | ⬜ Pendiente |
| `CallerIdentifier` | Identifica al llamante vs. base de datos | ⬜ Pendiente |
| `AiEngine` | Pipeline de transcripción + clasificación | ⬜ Pendiente |
| `RoutingEngine` | Aplica reglas PBX configurables | ⬜ Pendiente |
| `CallUI` | Pantalla de llamada entrante con contexto | ⬜ Pendiente |
| `ContactsModule` | Gestión de agenda y perfiles de llamantes | ⬜ Pendiente |
| `SettingsModule` | Configuración de reglas y preferencias | ⬜ Pendiente |

---

## 5. Parámetros Técnicos Actuales

| Parámetro | Valor Actual | Notas |
|-----------|-------------|-------|
| `minSdkVersion` | _[pendiente]_ | |
| `targetSdkVersion` | _[pendiente]_ | |
| `Kotlin version` | _[pendiente]_ | |
| `Gradle version` | _[pendiente]_ | |
| `SIP Library` | _[por definir: Linphone / PJSIP / WebRTC]_ | |
| `AI/NLP Backend` | _[por definir: Whisper / Gemini / OpenAI]_ | |
| `Codec de audio` | _[por definir: G.711 / G.722 / Opus]_ | |

---

## 6. Decisiones de Arquitectura

| Decisión | Opción Elegida | Alternativas Consideradas | Razón |
|----------|---------------|--------------------------|-------|
| _[Claude registra cada decisión técnica tomada]_ | | | |

---

## 7. Historial de Cambios

| Fecha | Cambio | Módulo Afectado |
|-------|--------|----------------|
| _[Claude registra cada modificación aquí]_ | | |

---

## 8. Issues y Deuda Técnica

| ID | Descripción | Prioridad | Estado |
|----|-------------|-----------|--------|
| _[Claude registra problemas detectados durante el desarrollo]_ | | | |

---

## 9. Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| Archivos `.kt` | _[Claude cuenta]_ |
| Líneas de código (LOC) | _[Claude estima]_ |
| Cobertura de tests | _[Claude calcula]_ |
| Módulos completados | 0 / 8 |
| Features implementadas | 0 |
