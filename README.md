# 📓 MvvmLab — Ejercicio 3: Primer ViewModel, sin magia

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
| 👉 **[`ej03-viewmodel`](../../tree/ej03-viewmodel)** | **3 — Primer ViewModel, sin magia** | ViewModel, rotación vs. muerte del proceso |
| [`ej04-stateflow`](../../tree/ej04-stateflow) | 4 — StateFlow: la pantalla observa | StateFlow + collectAsStateWithLifecycle |
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
         ├── NotaViewModel.kt  # ViewModel: estado con mutableStateOf + private set, contador de intentos
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
| Presentation | `presentation/nota/NotaViewModel.kt` | ViewModel: estado con `mutableStateOf` + `private set`, contador de intentos |
| Presentation | `presentation/nota/PantallaNota.kt` | View sin estado + conexión con el ViewModel + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Mover la lógica del ejercicio anterior a una clase que extienda `ViewModel`. La pantalla recibe estado y
funciones de evento, sin decidir cómo se guarda una nota. Obtener el ViewModel con el mecanismo apropiado
y comprobar qué sucede al rotar el emulador.
**Desafío:** contador de intentos de guardado para observar la vida del ViewModel.

## 🔑 Puntos clave

- `class NotaViewModel : ViewModel()` — hereda de `ViewModel`, por eso **sobrevive a la rotación**.
- `var estado by mutableStateOf(NotaUiState())` + **`private set`** — la pantalla lo lee, pero solo el ViewModel lo cambia.
- `onTituloChange`, `onContenidoChange`, `onGuardar` — las reglas del Ej 2, ahora como métodos del ViewModel.
- `viewModelScope.launch { delay(1500) ... }` — guardado simulado; si se rota a mitad, sigue y termina.
- `init { Log.d(...) }` / `onCleared()` — permiten ver en **Logcat** (filtro `NotaViewModel`) cuándo nace y muere.
- `private val notaViewModel: NotaViewModel by viewModels()` — devuelve **el mismo** ViewModel tras la rotación. Nunca `NotaViewModel()` a mano.
- No se agregaron dependencias: `viewModels()` y `viewModelScope` ya venían con `activity-compose` / `lifecycle`.

## 🔬 Experimentos (emulador Pixel 8)

| Prueba | Resultado observado |
|---|---|
| Rotar con la versión del Ej 2 (`remember`) | Título y contenido **se borran** |
| Rotar con el ViewModel | Se conservan título, contenido, nota guardada e **intentos: 2**. `ViewModel CREADO` aparece **una sola vez** en Logcat |
| App en segundo plano + `adb shell am kill` y volver | Pantalla **vacía**, intentos: 0. Logcat: un segundo `ViewModel CREADO` con otro id |

## ❓ Preguntas de la guía

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

## ✅ Criterio de finalización
- ✅ La Activity no contiene validación de notas.
- ✅ El ViewModel no contiene llamadas a composables.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej03-viewmodel
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
