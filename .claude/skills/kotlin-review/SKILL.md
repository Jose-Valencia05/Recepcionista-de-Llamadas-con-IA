---
name: kotlin-review
description: >
  Revisa código Kotlin/Android. Úsala cuando el usuario pida revisar,
  auditar, o mejorar código Kotlin, Composables, ViewModels, UseCases,
  o Services de Android. También actívala si detectas anti-patrones
  como lógica de negocio en Composables, callbacks en lugar de coroutines,
  o ausencia de manejo de errores.
---

# Skill: Revisión de Código Kotlin/Android

## Protocolo de Revisión

Cuando se invoca esta skill, analiza el código en el siguiente orden:

### 1. Arquitectura (¿Respeta Clean Architecture + MVVM?)
- ¿Los Composables solo renderizan `UiState` y emiten eventos?
- ¿Los ViewModels usan UseCases, no Repositories directamente?
- ¿Los UseCases son de propósito único (Single Responsibility)?
- ¿Los Services se comunican vía SharedFlow / eventos, no directamente?

### 2. Kotlin Idiomático
Busca y señala:
- **❌ Anti-patrón:** Null checks con `if (x != null)` → **✅ Usar:** `x?.let { }` o `requireNotNull()`
- **❌ Anti-patrón:** Callbacks legacy → **✅ Usar:** `suspendCoroutine {}` o `callbackFlow {}`
- **❌ Anti-patrón:** `class` para data holders → **✅ Usar:** `data class`
- **❌ Anti-patrón:** `when` sin `else` en sealed classes → **✅ Cubrir todos los casos**
- **❌ Anti-patrón:** Lógica en `init {}` de ViewModel → **✅ Mover a funciones explícitas**

### 3. Manejo de Errores
- ¿Las operaciones de red/SIP retornan `Result<T>` o un `sealed class` de estado?
- ¿Hay `try/catch` genérico que silencia errores?
- ¿Los errores llegan al `UiState` para que el usuario sea informado?

### 4. Ciclo de Vida Android
- ¿Hay coroutines lanzadas sin `viewModelScope` o `lifecycleScope`?
- ¿Hay referencias a `Context` en ViewModel? (memory leak)
- ¿El foreground service tiene el `startForeground()` en el lugar correcto?

### 5. Performance
- ¿Hay operaciones de red/IO en el hilo principal?
- ¿Los `Flow` usan el `Dispatcher` correcto? (`IO` para disco/red, `Default` para CPU)
- ¿Los Composables tienen `remember {}` donde corresponde?

## Formato de Salida

Para cada problema encontrado, usa este formato:

```
🔴 CRÍTICO / 🟡 MEJORA / 🟢 SUGERENCIA

Línea X: [Descripción del problema]
Código actual:
  [fragmento]
Código sugerido:
  [fragmento corregido]
Razón: [Por qué es mejor — desde primeros principios]
```

Al final, incluye un **resumen de puntuación**:
```
Arquitectura: X/10
Kotlin idiomático: X/10
Manejo de errores: X/10
Ciclo de vida: X/10
```
