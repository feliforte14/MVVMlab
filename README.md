# 📓 MvvmLab — Ejercicio 5: Validar sin esconder la regla en un botón

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
| [`ej04-stateflow`](../../tree/ej04-stateflow) | 4 — StateFlow: la pantalla observa | StateFlow + collectAsStateWithLifecycle |
| 👉 **[`ej05-validacion`](../../tree/ej05-validacion)** | **5 — Validar sin esconder la regla en un botón** | Reglas en Kotlin puro, errores por campo |
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
 │   ├── model/
 │   │   └── Nota.kt           # Modelo de una nota
 │   └── validacion/           # reglas de negocio
 │       └── ValidadorNota.kt  # Reglas: título no vacío, contenido ≥ 10 caracteres
 └── presentation/             # MVVM: una carpeta por pantalla
     └── nota/
         ├── NotaUiState.kt    # Estado de pantalla con un error por campo
         ├── NotaViewModel.kt  # ViewModel: aplica las reglas y evita el doble guardado
         └── PantallaNota.kt   # View sin estado + conexión con el ViewModel + previews
```

```
app/src/test/java/com/example/mvvmlab/
 └── domain/                       # tests de reglas
     └── validacion/
         └── ValidadorNotaTest.kt  # 5 tests de las reglas
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
| Domain | `domain/validacion/ValidadorNota.kt` | Reglas: título no vacío, contenido ≥ 10 caracteres |
| Presentation | `presentation/nota/NotaUiState.kt` | Estado de pantalla con un error por campo |
| Presentation | `presentation/nota/NotaViewModel.kt` | ViewModel: aplica las reglas y evita el doble guardado |
| Presentation | `presentation/nota/PantallaNota.kt` | View sin estado + conexión con el ViewModel + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Agregar reglas: título no vacío, contenido de al menos diez caracteres y prohibición de guardar dos veces mientras se
procesa una solicitud. Ubicar las reglas fuera de la vista. Mostrar errores cerca de los campos.
**Desafío:** probar espacios en blanco, una nota válida y dos pulsaciones seguidas.

## 📋 Reglas

| Regla | Dónde vive |
|---|---|
| Título no vacío (`isBlank`: solo espacios cuenta como vacío) | `ValidadorNota` |
| Contenido ≥ 10 caracteres (`trim()` antes de contar) | `ValidadorNota` |
| No guardar dos veces mientras se procesa | `NotaViewModel.onGuardar` (el botón deshabilitado es solo ayuda visual) |

**¿Cuándo se limpia un error?** Al editar el campo que lo causó, y solo ese error.

## 🔬 Pruebas

- `ValidadorNotaTest`: 5/5 ✅ (solo espacios, espacios alrededor, borde de 10 caracteres, nota válida, ambos errores).
- Emulador: título `"   "` y contenido `"corto"` → cada error debajo de su campo; doble toque rápido → Logcat muestra **un solo** `guardando`.

## ❓ Preguntas de la guía

**¿Cuál es una regla de negocio y cuál es una decisión visual?**
Regla: "contenido ≥ 10 caracteres", "no guardar dos veces a la vez" (valen en cualquier interfaz).
Visual: error en rojo debajo del campo, botón deshabilitado, texto "Guardando...".

**¿En qué momento conviene limpiar un mensaje de error?**
Cuando el usuario modifica el campo que lo causó, y solo ese error.

## ✅ Criterio de finalización
- ✅ Se cumplen las validaciones y el botón sólo comunica una intención al ViewModel.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej05-validacion
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
