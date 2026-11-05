# MvvmLab — Ejercicio 1: ¿Quién está haciendo demasiado?

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Kotlin + Jetpack Compose + Material 3.

📄 Apuntes (teoría + resolución de los ejercicios hasta este): [`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

## Ramas

Cada rama contiene **solo el código de su ejercicio** (la app evoluciona de una rama a la siguiente).

| Rama | Ejercicio |
|---|---|
| `main` | Proyecto base (Empty Activity Compose) |
| **`ej01-responsabilidades`** | **1 — ¿Quién está haciendo demasiado?** ← esta rama |

---

## Estructura de carpetas

Organizado por capas como *PokedexAcademy-MVVM-Clean*: `domain/` (Kotlin puro: modelos, contratos, reglas), `data/` (implementaciones), `presentation/` (ViewModels y pantallas), `di/`, `core/`.

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt
 ├── core/
 │   ├── theme/
 │   │   ├── Color.kt
 │   │   ├── Theme.kt
 │   │   ├── Type.kt
 ├── presentation/
 │   ├── tareas/
 │   │   ├── PantallaTareas.kt

app/src/test/java/com/example/mvvmlab/
 ├── ExampleUnitTest.kt
```

## Consigna

Tomar una pantalla donde todo está mezclado, marcar dónde se dibuja la interfaz, dónde se validan los datos,
dónde se modifica la lista y dónde se guarda, y ubicar cada responsabilidad en **View**, **ViewModel** o
**Model/Repository**. Todavía **no** se cambia la implementación.

**Archivo:** `MainActivity.kt` → `PantallaTareas`: pantalla de tareas con todo mezclado, *a propósito*, para analizarla.

## Tabla de responsabilidades (entregable)

| Letra | Código | Qué hace | Dónde va |
|---|---|---|---|
| (A) | `var texto` | Borrador que escribe el usuario; no sobrevive al cierre | ViewModel |
| (B) | `var tareas` | Lista real de tareas; tiene que sobrevivir al cierre | Repository |
| (C) | `var error` | Mensaje de error actual | ViewModel |
| (D) | `Column`, `Text`, `OutlinedTextField` | Dibuja la pantalla | View |
| (E) | `texto.isBlank()` | Regla: no vacía | ViewModel |
| (F) | `tareas.contains(texto)` | Regla: no repetida | ViewModel |
| (G) | `tareas = tareas + texto` | Agrega la tarea | Repository |
| (H) | `File(...).writeText(...)` | Guarda en archivo | Repository |
| (I) | `texto = ""` | Limpia el campo si se agregó bien | ViewModel |
| (J) | `error?.let { Text(it, Color.Red) }` | Muestra el error | View |
| (K) | `LazyColumn` | Muestra la lista | View |

**Criterio para decidir:** ¿dibuja algo? → View · ¿es una regla que vale en cualquier interfaz? → ViewModel ·
¿el dato tiene que sobrevivir si cierro la app? → Repository.
**El ViewModel decide QUÉ mostrar; la View decide CÓMO mostrarlo.**

## Desafío: las dos flechas (UDF)

```
            EVENTO (sube)                      operación
  View  ─────────────────────►  ViewModel  ──────────────►  Repository
        "apretó Agregar"                     "agregá esta tarea"

            ESTADO (baja)
  View  ◄─────────────────────  ViewModel
        texto = "", error = null, tareas = [...]
```

## Preguntas de la guía

**¿Qué trabajo tendría que seguir funcionando aunque mañana cambiemos Compose por otra interfaz?**
Las reglas (E, F) y el manejo de datos (G, H): todo lo que va en ViewModel y Repository.

**¿Qué datos existen sólo mientras se está escribiendo un formulario?**
`texto` (el borrador) y `error` (el mensaje actual): estado de pantalla, no se guardan.

## Criterio de finalización
- ✅ El esquema identifica 11 responsabilidades y propone dónde ubicar cada una.
