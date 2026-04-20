# 🛠️ FICHA_TECNICA.md
# AI Call Receptionist — Ficha Técnica del Código

> ⚙️ **Documento Vivo:** Actualizado automáticamente por Claude Code tras cada
> modificación de código. Refleja el estado actual del codebase en tiempo real.
> Última actualización: _[Claude actualiza este campo en cada sync]_

---

## 1. Stack Tecnológico

| Tecnología | Versión | Rol |
|-----------|---------|-----|
| Kotlin | _[pendiente]_ | Lenguaje principal |
| Android SDK | _[pendiente]_ | Plataforma base |
| Jetpack Compose | _[pendiente]_ | UI declarativa |
| Hilt | _[pendiente]_ | Inyección de dependencias |
| Coroutines + Flow | _[pendiente]_ | Async / reactividad |
| SIP Library | _[por definir]_ | Señalización VoIP |
| AI/NLP Engine | _[por definir]_ | Procesamiento de lenguaje |
| JUnit 5 + MockK | _[pendiente]_ | Testing |

---

## 2. Estructura de Paquetes

```
com.josedmv.aireceptionist/
│
├── core/               ← [Claude documenta clases aquí cuando existan]
├── data/               ← [Repositories, DataSources, DTOs]
├── domain/             ← [UseCases, Models, Interfaces]
├── presentation/       ← [ViewModels, Composables, UiState]
├── service/            ← [CallManager, SipService]
├── ai/                 ← [AiEngine, NLP pipeline]
└── di/                 ← [Módulos Hilt]
```

---

## 3. Inventario de Clases y Archivos

_Claude actualiza esta tabla con cada archivo creado o modificado_

| Clase / Archivo | Paquete | Tipo | Responsabilidad | Líneas |
|----------------|---------|------|----------------|--------|
| _[pendiente — se poblará con el primer archivo de código]_ | | | | |

---

## 4. Modelos de Datos (Data Classes / Sealed Classes)

_Claude documenta cada modelo cuando es creado_

```kotlin
// [Los modelos se documentarán aquí cuando existan]
```

---

## 5. Interfaces y Contratos

_Claude documenta cada interface del dominio_

| Interface | Métodos | Implementaciones |
|-----------|---------|-----------------|
| _[pendiente]_ | | |

---

## 6. UseCases Implementados

| UseCase | Input | Output | Descripción |
|---------|-------|--------|-------------|
| _[pendiente]_ | | | |

---

## 7. Flujos Reactivos (Flows / LiveData)

| Flow | Tipo | Origen | Consumidores |
|------|------|--------|-------------|
| _[pendiente]_ | | | |

---

## 8. Algoritmos Clave

_Claude documenta cada algoritmo no trivial_

| Algoritmo | Archivo | Complejidad | Descripción |
|-----------|---------|------------|-------------|
| _[pendiente]_ | | | |

---

## 9. Permisos de Android Utilizados

| Permiso | Usado en | Propósito |
|---------|----------|-----------|
| `READ_PHONE_STATE` | _[pendiente]_ | Estado de llamada |
| `CALL_PHONE` | _[pendiente]_ | Iniciar llamadas |
| `RECORD_AUDIO` | _[pendiente]_ | Captura de audio para IA |
| `FOREGROUND_SERVICE` | _[pendiente]_ | Servicio persistente |
| `FOREGROUND_SERVICE_PHONE_CALL` | _[pendiente]_ | Tipo específico (Android 14+) |

---

## 10. Métricas del Código

| Métrica | Valor |
|---------|-------|
| Total de archivos `.kt` | 0 |
| Total LOC | 0 |
| Data classes | 0 |
| Interfaces | 0 |
| UseCases | 0 |
| ViewModels | 0 |
| Services | 0 |
| Tests unitarios | 0 |
| Cobertura estimada | 0% |

---

## 11. Dependencias Gradle

_Claude actualiza esta sección cuando se agreguen dependencias al `build.gradle`_

```kotlin
// build.gradle.kts — dependencias actuales
// [Claude insertará las dependencias reales aquí]
```

---

## 12. Trazabilidad de Cambios

| Fecha | Archivo Modificado | Tipo de Cambio | Descripción |
|-------|--------------------|---------------|-------------|
| _[Claude registra cada cambio de código aquí]_ | | | |
