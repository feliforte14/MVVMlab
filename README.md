# MvvmLab — Ejercicio 5: Validar sin esconder la regla en un botón

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Kotlin + Jetpack Compose + Material 3.

📄 Apuntes (teoría + resolución de los ejercicios hasta este): [`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

## Ramas

Cada rama contiene **solo el código de su ejercicio** (la app evoluciona de una rama a la siguiente).

| Rama | Ejercicio |
|---|---|
| `main` | Proyecto base (Empty Activity Compose) |
| `ej01-responsabilidades` | 1 — ¿Quién está haciendo demasiado? |
| `ej02-estado-pantalla` | 2 — Una pantalla no es su estado |
| `ej03-viewmodel` | 3 — Primer ViewModel, sin magia |
| `ej04-stateflow` | 4 — StateFlow: la pantalla observa |
| **`ej05-validacion`** | **5 — Validar sin esconder la regla en un botón** ← esta rama |

---

## Consigna

Agregar reglas: título no vacío, contenido de al menos diez caracteres y prohibición de guardar dos veces mientras se
procesa una solicitud. Ubicar las reglas fuera de la vista. Mostrar errores cerca de los campos.
**Desafío:** probar espacios en blanco, una nota válida y dos pulsaciones seguidas.

## Archivos

| Archivo | Capa | Cambio |
|---|---|---|
| `ValidadorNota.kt` | Reglas (Kotlin puro) | **Nuevo**: `ValidadorNota.validar(titulo, contenido): ErroresNota` |
| `NotaViewModel.kt` | ViewModel | `onGuardar` aplica las reglas; `if (guardando) return` evita el doble guardado |
| `NotaUiState.kt` | Estado | `error` → `errorTitulo` + `errorContenido` (un error por campo) |
| `PantallaNota.kt` | View | Cada campo muestra su error con `isError` + `supportingText` |
| `app/src/test/.../ValidadorNotaTest.kt` | Test | **Nuevo**: 5 tests JUnit (sin emulador) |

## Reglas

| Regla | Dónde vive |
|---|---|
| Título no vacío (`isBlank`: solo espacios cuenta como vacío) | `ValidadorNota` |
| Contenido ≥ 10 caracteres (`trim()` antes de contar) | `ValidadorNota` |
| No guardar dos veces mientras se procesa | `NotaViewModel.onGuardar` (el botón deshabilitado es solo ayuda visual) |

**¿Cuándo se limpia un error?** Al editar el campo que lo causó, y solo ese error.

## Pruebas

- `ValidadorNotaTest`: 5/5 ✅ (solo espacios, espacios alrededor, borde de 10 caracteres, nota válida, ambos errores).
- Emulador: título `"   "` y contenido `"corto"` → cada error debajo de su campo; doble toque rápido → Logcat muestra **un solo** `guardando`.

## Preguntas de la guía

**¿Cuál es una regla de negocio y cuál es una decisión visual?**
Regla: "contenido ≥ 10 caracteres", "no guardar dos veces a la vez" (valen en cualquier interfaz).
Visual: error en rojo debajo del campo, botón deshabilitado, texto "Guardando...".

**¿En qué momento conviene limpiar un mensaje de error?**
Cuando el usuario modifica el campo que lo causó, y solo ese error.

## Criterio de finalización
- ✅ Se cumplen las validaciones y el botón sólo comunica una intención al ViewModel.
