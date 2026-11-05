# MvvmLab — Ejercicio 6: Repository, cambiar datos sin cambiar la pantalla

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
| `ej05-validacion` | 5 — Validar sin esconder la regla en un botón |
| **`ej06-repository`** | **6 — Repository: cambiar datos sin cambiar la pantalla** ← esta rama |

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
 ├── data/
 │   ├── repository/
 │   │   ├── NotaRepositoryDeEjemplo.kt
 │   │   ├── NotaRepositoryEnMemoria.kt
 ├── domain/
 │   ├── model/
 │   │   ├── Nota.kt
 │   ├── repository/
 │   │   ├── NotaRepository.kt
 │   ├── validacion/
 │   │   ├── ValidadorNota.kt
 ├── presentation/
 │   ├── nota/
 │   │   ├── NotaUiState.kt
 │   │   ├── NotaViewModel.kt
 │   │   ├── PantallaNota.kt

app/src/test/java/com/example/mvvmlab/
 ├── domain/
 │   ├── validacion/
 │   │   ├── ValidadorNotaTest.kt
```

## Consigna

Definir un contrato `NotaRepository` con operaciones para obtener, guardar y eliminar notas. Implementar primero una
versión en memoria. El ViewModel sólo habla con el contrato. Decidir valor puntual vs `Flow` y justificarlo.
**Desafío:** reemplazar la implementación por otra lista precargada sin modificar el composable.

## Archivos

| Archivo | Capa | Cambio |
|---|---|---|
| `domain/repository/NotaRepository.kt` | Datos | **Nuevo**: `interface NotaRepository` + `NotaRepositoryEnMemoria` + `NotaRepositoryDeEjemplo` (desafío) |
| `presentation/nota/NotaViewModel.kt` | ViewModel | Recibe `NotaRepository` por constructor; carga en `init`, `onGuardar` y `onEliminar` delegan al Repository |
| `presentation/nota/NotaUiState.kt` | Estado | `Nota` con `id`; estado con `notas: List<Nota>` |
| `presentation/nota/PantallaNota.kt` | View | `LazyColumn` con formulario + lista; `FilaNota` reutilizable; callback `onEliminar` |

## Contrato

```kotlin
interface NotaRepository {
    suspend fun obtenerTodas(): List<Nota>
    suspend fun guardar(nota: Nota)      // si ya existe ese id, la reemplaza
    suspend fun eliminar(id: String)
}

class NotaViewModel(
    private val repositorio: NotaRepository = NotaRepositoryEnMemoria()   // único lugar donde se elige
) : ViewModel()
```

## Decisión: valor puntual (`suspend`) y no `Flow`

Devuelve una "foto" de las notas; después de guardar o eliminar, el ViewModel vuelve a pedirla (`recargarNotas()`).
Es simple y alcanza con **una sola pantalla**. `Flow` conviene cuando varias pantallas observan los mismos datos o la
fuente ya emite cambios (Room) → se adopta en el Ej 12.

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| Agregar 2 notas | "Notas guardadas (2)" ✅ |
| Eliminar una | "Notas guardadas (1)" ✅ |
| Cambiar una línea a `NotaRepositoryDeEjemplo()` | 3 notas precargadas, `presentation/nota/PantallaNota.kt` sin cambios ✅ |

## Preguntas de la guía

**¿Qué conoce el Repository sobre Compose?** Nada: recibe y devuelve `Nota` (Kotlin puro).

**¿Por qué esta interfaz ayuda a probar la app?** El ViewModel depende del contrato: en un test se le pasa un
Repository falso (datos fijos o fallas a propósito) sin archivos ni base de datos (Ej 13).

## Criterio de finalización
- ✅ La implementación concreta se puede cambiar sin reescribir la pantalla.
