# 📓 MvvmLab — Ejercicio 2: Una pantalla no es su estado

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
| 👉 **[`ej02-estado-pantalla`](../../tree/ej02-estado-pantalla)** | **2 — Una pantalla no es su estado** | Estado único, eventos, state hoisting |
| [`ej03-viewmodel`](../../tree/ej03-viewmodel) | 3 — Primer ViewModel, sin magia | ViewModel, rotación vs. muerte del proceso |
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
 ├── MainActivity.kt          # Punto de entrada: muestra la pantalla
 ├── core/                    # comunes: tema, estados, rutas, utilidades
 │   └── theme/
 │       ├── Color.kt         # Colores del tema
 │       ├── Theme.kt         # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt          # Tipografías
 ├── domain/                  # QUÉ es la app: Kotlin puro, sin Android
 │   └── model/
 │       └── Nota.kt          # Una nota ya guardada (título, contenido)
 └── presentation/            # MVVM: una carpeta por pantalla
     └── nota/
         ├── NotaEventos.kt   # Eventos como funciones puras: estado → nuevo estado
         ├── NotaUiState.kt   # Estado de pantalla (una sola data class)
         └── PantallaNota.kt  # View sin estado + dueño provisorio del estado (remember) + previews
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
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Domain | `domain/model/Nota.kt` | Una nota ya guardada (título, contenido) |
| Presentation | `presentation/nota/NotaEventos.kt` | Eventos como funciones puras: estado → nuevo estado |
| Presentation | `presentation/nota/NotaUiState.kt` | Estado de pantalla (una sola `data class`) |
| Presentation | `presentation/nota/PantallaNota.kt` | View sin estado + dueño provisorio del estado (`remember`) + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Pantalla de notas con **título**, **contenido** y botón **Guardar** (guardado *simulado* con `delay`).
Todavía **sin ViewModel**: el objetivo es que la pantalla se pueda dibujar a partir de **un único estado**.

## 📋 Estado de pantalla

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

## 📋 Tabla evento → cambio de estado (desafío)

| Evento | Cambio de estado esperado |
|---|---|
| Edita el **título** | `titulo = nuevo`, `error = null` (el error deja de tener sentido al escribir) |
| Edita el **contenido** | `contenido = nuevo` |
| Pulsa **Guardar** con título vacío | `error = "El título no puede estar vacío"` |
| Pulsa **Guardar** con título válido | `guardando = true`, `error = null` |
| Pulsa **Guardar** mientras ya guarda | sin cambios (se ignora; además el botón está deshabilitado) |
| **Termina** el guardado simulado | `guardando = false`, `guardada = Nota(titulo, contenido)` |

## 📋 Estructura del código

| Pieza | Rol |
|---|---|
| `Nota` | Modelo: una nota ya guardada |
| `NotaUiState` | Estado de pantalla: **qué** se ve |
| `alEditarTitulo`, `alEditarContenido`, `alPulsarGuardar`, `alTerminarGuardado` | Funciones Kotlin puras *estado → nuevo estado*. No conocen Compose; en el Ej 3 pasan al ViewModel |
| `PantallaNotaConEstado` | Dueño del estado (provisorio) y del guardado simulado |
| `PantallaNota` | View **sin estado**: dibuja `NotaUiState` y avisa eventos por callbacks (*state hoisting*) |
| 4 `@Preview` | Vacía · Error · Guardando · Guardada con cambios sin guardar |

## ❓ Preguntas de la guía

**¿El texto ingresado es lo mismo que la nota ya guardada?**
No. `titulo`/`contenido` son lo que el usuario **está escribiendo** (puede cambiar en cada tecla);
`guardada` es la **última versión confirmada**. Por eso son campos distintos, y la diferencia entre ambos
es justamente lo que permite mostrar "Hay cambios sin guardar".

**¿Conviene mostrar el mensaje de error como parte del estado?**
Sí. Si el error está en el estado, la pantalla se puede reconstruir completa a partir de `NotaUiState`
(UI = f(estado)): sobrevive a la recomposición, se puede previsualizar y, en el Ej 3, también a la
rotación. Si fuera un mensaje "suelto" (un Toast disparado desde el botón), no habría forma de saber
si se está mostrando ni de recuperarlo.

## ✅ Criterio de finalización
- ✅ Eventos reconocibles: `onTituloChange`, `onContenidoChange`, `onGuardar`.
- ✅ La pantalla se representa a partir de un único estado definido (`NotaUiState`): lo demuestran las 4 previews, que dibujan cualquier situación sin ejecutar la app.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej02-estado-pantalla
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
