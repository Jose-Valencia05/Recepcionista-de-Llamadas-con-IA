---
name: doc-sync
description: >
  Actualiza la documentación viva del proyecto después de cualquier
  modificación de código. Úsala automáticamente después de crear,
  editar o eliminar archivos Kotlin, XML de layout, build.gradle,
  o cualquier archivo del proyecto. También se activa si el usuario
  dice "actualiza los docs", "sincroniza la documentación" o "doc-sync".
  SIEMPRE ejecutar al final de cada sesión de trabajo.
---

# Skill: doc-sync — Documentación Viva

## Propósito

Este skill es el "notario del proyecto": después de cada modificación,
revisa el estado real del codebase y actualiza `docs/PROJECT_PROGRESS.md`
y `docs/FICHA_TECNICA.md` para que siempre reflejen la verdad del código.

---

## Protocolo de Ejecución

### Paso 1 — Escanear el proyecto

Ejecuta estos análisis **antes** de actualizar los docs:

```bash
# Contar archivos Kotlin
find . -name "*.kt" | grep -v build | wc -l

# Listar todos los archivos .kt con sus rutas
find . -name "*.kt" | grep -v build | sort

# Contar líneas de código
find . -name "*.kt" | grep -v build | xargs wc -l | tail -1

# Ver dependencias actuales del build.gradle
cat app/build.gradle.kts 2>/dev/null || cat app/build.gradle 2>/dev/null

# Ver versiones en libs.versions.toml
cat gradle/libs.versions.toml 2>/dev/null
```

### Paso 2 — Analizar los cambios recientes

Identifica qué cambió en esta sesión:
- ¿Qué archivos fueron creados, editados o eliminados?
- ¿Se agregaron nuevas clases, interfaces, UseCases, ViewModels?
- ¿Se agregaron dependencias nuevas al Gradle?
- ¿Se tomaron decisiones de arquitectura o tecnología?
- ¿Se detectaron issues o deuda técnica?

### Paso 3 — Actualizar PROJECT_PROGRESS.md

En `docs/PROJECT_PROGRESS.md`, actualiza **sin cambiar el formato**:

1. **"Última actualización"** → fecha y hora actual
2. **Versión** → incrementar si hubo cambio significativo
3. **Estado del proyecto** → emoji + descripción
4. **Barras de progreso** → recalcular % real por módulo
5. **Tabla de Módulos** → cambiar estado (⬜ Pendiente / 🟡 En progreso / ✅ Completo)
6. **Parámetros Técnicos** → llenar/actualizar versiones reales del Gradle
7. **Decisiones de Arquitectura** → agregar fila si se tomó alguna decisión nueva
8. **Historial de Cambios** → agregar fila con fecha + cambio + módulo
9. **Issues y Deuda Técnica** → agregar si se detectaron durante el trabajo
10. **Métricas** → actualizar contadores reales

### Paso 4 — Actualizar FICHA_TECNICA.md

En `docs/FICHA_TECNICA.md`, actualiza **sin cambiar el formato**:

1. **"Última actualización"** → fecha y hora actual
2. **Stack Tecnológico** → versiones reales del Gradle
3. **Estructura de Paquetes** → refleja los paquetes reales que existen
4. **Inventario de Clases** → agregar/actualizar cada `.kt` con su responsabilidad
5. **Modelos de Datos** → documentar data classes y sealed classes reales
6. **Interfaces y Contratos** → documentar interfaces del dominio
7. **UseCases** → agregar cada UseCase con su Input/Output
8. **Flujos Reactivos** → documentar Flows/StateFlow activos
9. **Algoritmos Clave** → documentar lógica no trivial
10. **Dependencias Gradle** → copiar las dependencias reales del build.gradle
11. **Métricas** → recalcular con `find` y `wc -l`
12. **Trazabilidad** → agregar fila por cada archivo modificado

---

## Reglas Críticas

1. **NUNCA alterar el formato** de los documentos — solo el contenido dentro de las tablas/secciones
2. **NUNCA inventar** datos — solo registrar lo que realmente existe en el código
3. **SIEMPRE** actualizar el campo "Última actualización" con la fecha real
4. Si una sección no tiene cambios, **dejarla igual** (no borrarla)
5. Las métricas deben ser **contadas realmente** con bash, no estimadas

---

## Confirmación de Ejecución

Al terminar, reporta:

```
✅ doc-sync completado
📄 PROJECT_PROGRESS.md → [N cambios realizados]
📄 FICHA_TECNICA.md    → [N cambios realizados]
📊 Estado del proyecto: [descripción breve]
```
