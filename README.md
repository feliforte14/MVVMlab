# MvvmLab — Ejercicio 3: Primer ViewModel, sin magia

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Kotlin + Jetpack Compose + Material 3.

📄 Apuntes (teoría + resolución de los ejercicios hasta este): [`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

## Ramas

Cada rama contiene **solo el código de su ejercicio**.

| Rama | Ejercicio |
|---|---|
| `main` | Proyecto base (Empty Activity Compose) |
| `ej01-responsabilidades` | 1 — ¿Quién está haciendo demasiado? |
| `ej02-estado-pantalla` | 2 — Una pantalla no es su estado |
| **`ej03-viewmodel`** | **3 — Primer ViewModel, sin magia** ← esta rama |

---

## Consigna

Mover la lógica del ejercicio anterior a una clase que extienda `ViewModel`. La pantalla recibe estado y
funciones de evento, sin decidir cómo se guarda una nota. Obtener el ViewModel con el mecanismo apropiado
y comprobar qué sucede al rotar el emulador.
**Desafío:** contador de intentos de guardado para observar la vida del ViewModel.

## Archivos

| Archivo | Capa | Contenido |
|---|---|---|
| `NotaUiState.kt` | Modelo / estado | `Nota` (nota guardada) y `NotaUiState` (estado de pantalla) |
| `PantallaNota.kt` | View | Composable **sin estado**: dibuja `NotaUiState` y avisa eventos por callbacks. 3 previews |
| `NotaViewModel.kt` | ViewModel | Dueño del estado y de las reglas; `PantallaNotaConViewModel` conecta VM ↔ View |
| `MainActivity.kt` | — | Obtiene el ViewModel con `by viewModels()` y lo pasa a la pantalla. No valida nada |

## Puntos clave

- `class NotaViewModel : ViewModel()` — hereda de `ViewModel`, por eso **sobrevive a la rotación**.
- `var estado by mutableStateOf(NotaUiState())` + **`private set`** — la pantalla lo lee, pero solo el ViewModel lo cambia.
- `onTituloChange`, `onContenidoChange`, `onGuardar` — las reglas del Ej 2, ahora como métodos del ViewModel.
- `viewModelScope.launch { delay(1500) ... }` — guardado simulado; si se rota a mitad, sigue y termina.
- `init { Log.d(...) }` / `onCleared()` — permiten ver en **Logcat** (filtro `NotaViewModel`) cuándo nace y muere.
- `private val notaViewModel: NotaViewModel by viewModels()` — devuelve **el mismo** ViewModel tras la rotación. Nunca `NotaViewModel()` a mano.
- No se agregaron dependencias: `viewModels()` y `viewModelScope` ya venían con `activity-compose` / `lifecycle`.

## Experimentos (emulador Pixel 8)

| Prueba | Resultado observado |
|---|---|
| Rotar con la versión del Ej 2 (`remember`) | Título y contenido **se borran** |
| Rotar con el ViewModel | Se conservan título, contenido, nota guardada e **intentos: 2**. `ViewModel CREADO` aparece **una sola vez** en Logcat |
| App en segundo plano + `adb shell am kill` y volver | Pantalla **vacía**, intentos: 0. Logcat: un segundo `ViewModel CREADO` con otro id |

## Preguntas de la guía

**¿Qué problema resuelve el ViewModel?**
(a) Sobrevive a los **cambios de configuración** (rotación, idioma, modo oscuro): el estado de pantalla y las
operaciones en curso no se pierden cuando Android recrea la Activity. (b) **Saca la lógica de la UI**: las reglas
quedan en una clase que no dibuja ni conoce la Activity.

**¿Qué sucederá si el proceso se elimina completamente?**
El ViewModel vive en memoria dentro del proceso: si Android lo mata o el usuario cierra la app, muere con él y al
volver se crea uno nuevo, vacío. **Un ViewModel no es persistencia.** Para eso: `SavedStateHandle` /
`rememberSaveable` (datos chicos de pantalla) o un Repository con archivo / Room (Ej 6 en adelante).

| | Recomposición | Rotación | Muerte del proceso |
|---|---|---|---|
| `remember` | ✅ | ❌ | ❌ |
| `rememberSaveable` | ✅ | ✅ | ✅* |
| ViewModel | ✅ | ✅ | ❌ |
| Repository + archivo / Room | ✅ | ✅ | ✅ |

\* Datos chicos, y solo si el proceso lo mató Android (no si el usuario cerró la app).

## Criterio de finalización
- ✅ La Activity no contiene validación de notas.
- ✅ El ViewModel no contiene llamadas a composables.
