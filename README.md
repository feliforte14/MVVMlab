# MvvmLab — Ejercicio 2: Una pantalla no es su estado

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Kotlin + Jetpack Compose + Material 3.

📄 Apuntes (teoría + resolución de los ejercicios hasta este): [`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

## Ramas

Cada rama contiene **solo el código de su ejercicio** (la app evoluciona de una rama a la siguiente).

| Rama | Ejercicio |
|---|---|
| `main` | Proyecto base (Empty Activity Compose) |
| `ej01-responsabilidades` | 1 — ¿Quién está haciendo demasiado? |
| **`ej02-estado-pantalla`** | **2 — Una pantalla no es su estado** ← esta rama |

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
 ├── domain/
 │   ├── model/
 │   │   ├── Nota.kt
 ├── presentation/
 │   ├── nota/
 │   │   ├── NotaEventos.kt
 │   │   ├── NotaUiState.kt
 │   │   ├── PantallaNota.kt

app/src/test/java/com/example/mvvmlab/
 ├── ExampleUnitTest.kt
```

## Ejercicio 2 — Una pantalla no es su estado

**Archivo:** `domain/model/Nota.kt + presentation/nota/`

Pantalla de notas con **título**, **contenido** y botón **Guardar** (guardado *simulado* con `delay`).
Todavía **sin ViewModel**: el objetivo es que la pantalla se pueda dibujar a partir de **un único estado**.

### Estado de pantalla

```kotlin
data class NotaUiState(
    val titulo: String = "",          // lo escrito en el campo título
    val contenido: String = "",       // lo escrito en el campo contenido
    val error: String? = null,        // error de validación (null = sin error)
    val guardando: Boolean = false,   // operación en curso
    val guardada: Nota? = null        // última nota guardada (null = ninguna)
)
```

- Todos los campos son `val`: para cambiar el estado se crea uno nuevo con `copy(...)`.
- `hayCambiosSinGuardar` es una **propiedad calculada**: se deduce de los otros campos, no se duplica.

### Tabla evento → cambio de estado (desafío)

| Evento | Cambio de estado esperado |
|---|---|
| Edita el **título** | `titulo = nuevo`, `error = null` (el error deja de tener sentido al escribir) |
| Edita el **contenido** | `contenido = nuevo` |
| Pulsa **Guardar** con título vacío | `error = "El título no puede estar vacío"` |
| Pulsa **Guardar** con título válido | `guardando = true`, `error = null` |
| Pulsa **Guardar** mientras ya guarda | sin cambios (se ignora; además el botón está deshabilitado) |
| **Termina** el guardado simulado | `guardando = false`, `guardada = Nota(titulo, contenido)` |

### Estructura del código

| Pieza | Rol |
|---|---|
| `Nota` | Modelo: una nota ya guardada |
| `NotaUiState` | Estado de pantalla: **qué** se ve |
| `alEditarTitulo`, `alEditarContenido`, `alPulsarGuardar`, `alTerminarGuardado` | Funciones Kotlin puras *estado → nuevo estado*. No conocen Compose; en el Ej 3 pasan al ViewModel |
| `PantallaNotaConEstado` | Dueño del estado (provisorio) y del guardado simulado |
| `PantallaNota` | View **sin estado**: dibuja `NotaUiState` y avisa eventos por callbacks (*state hoisting*) |
| 4 `@Preview` | Vacía · Error · Guardando · Guardada con cambios sin guardar |

### Preguntas de la guía

**¿El texto ingresado es lo mismo que la nota ya guardada?**
No. `titulo`/`contenido` son lo que el usuario **está escribiendo** (puede cambiar en cada tecla);
`guardada` es la **última versión confirmada**. Por eso son campos distintos, y la diferencia entre ambos
es justamente lo que permite mostrar "Hay cambios sin guardar".

**¿Conviene mostrar el mensaje de error como parte del estado?**
Sí. Si el error está en el estado, la pantalla se puede reconstruir completa a partir de `NotaUiState`
(UI = f(estado)): sobrevive a la recomposición, se puede previsualizar y, en el Ej 3, también a la
rotación. Si fuera un mensaje "suelto" (un Toast disparado desde el botón), no habría forma de saber
si se está mostrando ni de recuperarlo.

### Criterio de finalización
- ✅ Eventos reconocibles: `onTituloChange`, `onContenidoChange`, `onGuardar`.
- ✅ La pantalla se representa a partir de un único estado definido (`NotaUiState`): lo demuestran las 4 previews, que dibujan cualquier situación sin ejecutar la app.
