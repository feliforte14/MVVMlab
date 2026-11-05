# 📓 MvvmLab — Ejercicio 4: StateFlow, la pantalla observa

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Aplicación Android en **Kotlin** con **Jetpack Compose** y **Material 3**, organizada por capas
(como [PokedexAcademy-MVVM-Clean](https://github.com/ExBattou/PokedexAcademy-MVVM-Clean)).

📄 **Apuntes** (teoría + resolución paso a paso de los ejercicios hasta este): [`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

---

## 🧭 Ramas

Cada rama contiene **solo el código de su ejercicio**; la app evoluciona de una rama a la siguiente.

| Rama | Ejercicio | Tema |
|---|---|---|
| [`main`](../../tree/main) | Proyecto base + apuntes completos |  |
| [`ej01-responsabilidades`](../../tree/ej01-responsabilidades) | 1 — ¿Quién está haciendo demasiado? | Responsabilidades View / ViewModel / Repository |
| [`ej02-estado-pantalla`](../../tree/ej02-estado-pantalla) | 2 — Una pantalla no es su estado | Estado único, eventos, state hoisting |
| [`ej03-viewmodel`](../../tree/ej03-viewmodel) | 3 — Primer ViewModel, sin magia | ViewModel, rotación vs. muerte del proceso |
| 👉 **[`ej04-stateflow`](../../tree/ej04-stateflow)** | **4 — StateFlow: la pantalla observa** | StateFlow + collectAsStateWithLifecycle |
| [`ej05-validacion`](../../tree/ej05-validacion) | 5 — Validar sin esconder la regla en un botón | Reglas en Kotlin puro, errores por campo |
| [`ej06-repository`](../../tree/ej06-repository) | 6 — Repository: cambiar datos sin cambiar la pantalla | Contrato e implementación en memoria |
| [`ej07-leer-archivo`](../../tree/ej07-leer-archivo) | 7 — Leer un archivo de texto desde el Repository | filesDir, Dispatchers.IO, resultado sellado |
| [`ej08-guardar-archivo`](../../tree/ej08-guardar-archivo) | 8 — Guardar texto, cerrar y volver | Escritura UTF-8, cambios sin guardar |
| [`ej09-carga-error`](../../tree/ej09-carga-error) | 9 — Cargando, listo o falló | Estados de carga/error, reintentar |
| [`ej10-lista-persistente`](../../tree/ej10-lista-persistente) | 10 — De un texto a una lista persistente | JSON, Mutex, escritura segura |
| [`ej11-dependencias`](../../tree/ej11-dependencias) | 11 — Dependencias: ¿quién crea el Repository? | Contenedor en Application |
| [`ej12-dos-pantallas`](../../tree/ej12-dos-pantallas) | 12 — Dos pantallas, una información | Flow como fuente de verdad, navegación |
| [`ej13-tests`](../../tree/ej13-tests) | 13 — Probar el ViewModel sin Android ni archivos | Fake repository, tests de ViewModel |
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador |
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt           # Obtiene el ViewModel con by viewModels() y muestra la pantalla
 ├── core/                     # comunes: tema, estados, rutas, utilidades
 │   └── theme/
 │       ├── Color.kt          # Colores del tema
 │       ├── Theme.kt          # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt           # Tipografías
 ├── domain/                   # QUÉ es la app: Kotlin puro, sin Android
 │   └── model/
 │       └── Nota.kt           # Modelo de una nota
 └── presentation/             # MVVM: una carpeta por pantalla
     └── nota/
         ├── NotaUiState.kt    # Estado de pantalla (una sola data class)
         ├── NotaViewModel.kt  # ViewModel con MutableStateFlow privado y StateFlow público
         └── PantallaNota.kt   # View sin estado + conexión con el ViewModel + previews
```

```
app/src/test/java/com/example/mvvmlab/
 └── ExampleUnitTest.kt  # Test de ejemplo de la plantilla
```

| Capa | Regla |
|---|---|
| **Domain** | Kotlin puro: modelos, contratos (interfaces) y reglas. No conoce Android, Compose, Room ni archivos. |
| **Data** | Implementa los contratos de Domain. Única capa que toca archivos, SQL o Room. |
| **Presentation** | ViewModels y pantallas Compose. Habla con los contratos, nunca con Data directamente. |
| **DI / Core** | DI conecta las piezas; Core tiene lo compartido (tema, estados, rutas). |

### 📂 Archivos por capa

| Capa | Archivo | Qué hace |
|---|---|---|
| Raíz | `MainActivity.kt` | Obtiene el ViewModel con `by viewModels()` y muestra la pantalla |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Domain | `domain/model/Nota.kt` | Modelo de una nota |
| Presentation | `presentation/nota/NotaUiState.kt` | Estado de pantalla (una sola `data class`) |
| Presentation | `presentation/nota/NotaViewModel.kt` | ViewModel con `MutableStateFlow` privado y `StateFlow` público |
| Presentation | `presentation/nota/PantallaNota.kt` | View sin estado + conexión con el ViewModel + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Exponer desde el ViewModel un `StateFlow` de sólo lectura y mantener mutable su implementación. En Compose
recolectarlo con `collectAsStateWithLifecycle`. Hacer que editar un campo cambie el estado visible.
**Desafío:** registrar cambios puntuales y comprobar que editar un campo no borra el otro.

## 🔑 Puntos clave

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

## 🔬 Experimento (Logcat, filtro `NotaViewModel`)

```
titulo -> titulo='Parcial DA'  contenido='Repasar StateFlow' error=null guardando=false
titulo -> titulo='Parcial DA1' contenido='Repasar StateFlow' error=null guardando=false
```
El contenido se mantiene mientras se edita el título. ✅

## ❓ Preguntas de la guía

**¿Por qué la pantalla no debería poder modificar directamente `_uiState`?**
Rompería el flujo unidireccional: el ViewModel dejaría de ser el único dueño del estado y la pantalla podría
saltearse las reglas. Si solo el ViewModel escribe, todo cambio pasa por un evento con nombre.

**¿Qué parte se recompone?**
`PantallaNotaConViewModel` (lee `estado`) y, dentro de `PantallaNota`, solo los elementos cuyos valores cambiaron;
los que reciben los mismos parámetros se saltean.

## ✅ Criterio de finalización
- ✅ Compose muestra el estado emitido y no mantiene una segunda copia de la nota.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej04-stateflow
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
