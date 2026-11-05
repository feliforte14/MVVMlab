# MvvmLab — Ejercicio 4: StateFlow, la pantalla observa

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
| **`ej04-stateflow`** | **4 — StateFlow: la pantalla observa** ← esta rama |

---

## Consigna

Exponer desde el ViewModel un `StateFlow` de sólo lectura y mantener mutable su implementación. En Compose
recolectarlo con `collectAsStateWithLifecycle`. Hacer que editar un campo cambie el estado visible.
**Desafío:** registrar cambios puntuales y comprobar que editar un campo no borra el otro.

## Archivos

| Archivo | Capa | Cambio respecto del Ej 3 |
|---|---|---|
| `NotaViewModel.kt` | ViewModel | `mutableStateOf` → `MutableStateFlow` privado + `StateFlow` público; eventos con `update { }`; logs |
| `NotaUiState.kt` | Estado | Sin cambios |
| `PantallaNota.kt` | View | Sin cambios |
| `gradle/libs.versions.toml`, `app/build.gradle.kts` | — | Dependencia `lifecycle-runtime-compose` 2.10.0 (misma versión de lifecycle que ya usaba el proyecto) |

## Puntos clave

```kotlin
private val _uiState = MutableStateFlow(NotaUiState())          // solo el VM escribe
val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()    // la pantalla solo lee

fun onTituloChange(nuevo: String) = _uiState.update { it.copy(titulo = nuevo, error = null) }

// En Compose:
val estado by viewModel.uiState.collectAsStateWithLifecycle()
```

- `update { }` es atómico y cada evento copia solo su campo → editar el título no borra el contenido.
- `collectAsStateWithLifecycle` deja de recolectar en segundo plano (`collectAsState` no).
- Compose no guarda una segunda copia de la nota: lee siempre la del ViewModel.

## Experimento (Logcat, filtro `NotaViewModel`)

```
titulo -> titulo='Parcial DA'  contenido='Repasar StateFlow' error=null guardando=false
titulo -> titulo='Parcial DA1' contenido='Repasar StateFlow' error=null guardando=false
```
El contenido se mantiene mientras se edita el título. ✅

## Preguntas de la guía

**¿Por qué la pantalla no debería poder modificar directamente `_uiState`?**
Rompería el flujo unidireccional: el ViewModel dejaría de ser el único dueño del estado y la pantalla podría
saltearse las reglas. Si solo el ViewModel escribe, todo cambio pasa por un evento con nombre.

**¿Qué parte se recompone?**
`PantallaNotaConViewModel` (lee `estado`) y, dentro de `PantallaNota`, solo los elementos cuyos valores cambiaron;
los que reciben los mismos parámetros se saltean.

## Criterio de finalización
- ✅ Compose muestra el estado emitido y no mantiene una segunda copia de la nota.
