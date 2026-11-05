# 📓 MvvmLab — Ejercicio 11: Dependencias, ¿quién crea el Repository?

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
| [`ej05-validacion`](../../tree/ej05-validacion) | 5 — Validar sin esconder la regla en un botón | Reglas en Kotlin puro, errores por campo |
| [`ej06-repository`](../../tree/ej06-repository) | 6 — Repository: cambiar datos sin cambiar la pantalla | Contrato e implementación en memoria |
| [`ej07-leer-archivo`](../../tree/ej07-leer-archivo) | 7 — Leer un archivo de texto desde el Repository | filesDir, Dispatchers.IO, resultado sellado |
| [`ej08-guardar-archivo`](../../tree/ej08-guardar-archivo) | 8 — Guardar texto, cerrar y volver | Escritura UTF-8, cambios sin guardar |
| [`ej09-carga-error`](../../tree/ej09-carga-error) | 9 — Cargando, listo o falló | Estados de carga/error, reintentar |
| [`ej10-lista-persistente`](../../tree/ej10-lista-persistente) | 10 — De un texto a una lista persistente | JSON, Mutex, escritura segura |
| 👉 **[`ej11-dependencias`](../../tree/ej11-dependencias)** | **11 — Dependencias: ¿quién crea el Repository?** | Contenedor en Application |
| [`ej12-dos-pantallas`](../../tree/ej12-dos-pantallas) | 12 — Dos pantallas, una información | Flow como fuente de verdad, navegación |
| [`ej13-tests`](../../tree/ej13-tests) | 13 — Probar el ViewModel sin Android ni archivos | Fake repository, tests de ViewModel |
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador |
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt                     # Obtiene el ViewModel con by viewModels { Factory } y muestra la pantalla
 ├── MvvmLabApp.kt                       # Application propia: crea el ContenedorApp una sola vez
 ├── core/                               # comunes: tema, estados, rutas, utilidades
 │   ├── model/
 │   │   └── EstadoCarga.kt              # Estados sellados de carga: Cargando / Listo / Error
 │   └── theme/
 │       ├── Color.kt                    # Colores del tema
 │       ├── Theme.kt                    # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt                     # Tipografías
 ├── data/                               # CÓMO se guardan los datos
 │   └── repository/
 │       ├── ArchivoNotasRepository.kt   # Guarda las notas en notas.json (JSON, Mutex, temporal + renombrado)
 │       └── NotaRepositoryEnMemoria.kt  # Implementación en memoria (se pierde al cerrar la app)
 ├── domain/                             # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── Nota.kt                     # Modelo de una nota
 │   ├── repository/
 │   │   └── NotaRepository.kt           # Contrato del Repository de notas
 │   └── validacion/                     # reglas de negocio
 │       └── ValidadorNota.kt            # Reglas: título no vacío, contenido ≥ 10 caracteres
 ├── presentation/                       # MVVM: una carpeta por pantalla
 │   └── notas/
 │       ├── NotasUiState.kt             # Estado: carga, lista y formulario (alta/edición)
 │       ├── NotasViewModel.kt           # Alta, edición y borrado a través del Repository; fábrica
 │       └── PantallaNotas.kt            # Formulario + lista con Editar / Eliminar
 └── di/                                 # quién crea las dependencias
     └── ContenedorApp.kt                # Crea una instancia de cada Repository (única decisión de configuración)
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
| Raíz | `MainActivity.kt` | Obtiene el ViewModel con `by viewModels { Factory }` y muestra la pantalla |
| Raíz | `MvvmLabApp.kt` | `Application` propia: crea el `ContenedorApp` una sola vez |
| Core | `core/model/EstadoCarga.kt` | Estados sellados de carga: `Cargando` / `Listo` / `Error` |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/repository/ArchivoNotasRepository.kt` | Guarda las notas en `notas.json` (JSON, `Mutex`, temporal + renombrado) |
| Data | `data/repository/NotaRepositoryEnMemoria.kt` | Implementación en memoria (se pierde al cerrar la app) |
| Domain | `domain/model/Nota.kt` | Modelo de una nota |
| Domain | `domain/repository/NotaRepository.kt` | Contrato del Repository de notas |
| Domain | `domain/validacion/ValidadorNota.kt` | Reglas: título no vacío, contenido ≥ 10 caracteres |
| Presentation | `presentation/notas/NotasUiState.kt` | Estado: carga, lista y formulario (alta/edición) |
| Presentation | `presentation/notas/NotasViewModel.kt` | Alta, edición y borrado a través del Repository; fábrica |
| Presentation | `presentation/notas/PantallaNotas.kt` | Formulario + lista con Editar / Eliminar |
| DI | `di/ContenedorApp.kt` | Crea una instancia de cada Repository (única decisión de configuración) |

---

<!-- ejercicio -->
## 🎯 Consigna

Revisar dónde se construyen el Repository y el ViewModel. Suministrar la dependencia desde la capa de aplicación
(una fábrica explícita alcanza). El ViewModel recibe el contrato, no crea archivos con un Context de la UI.
Justificar si se usa `AndroidViewModel`.
**Desafío:** alternar entre Repository en memoria y de archivos con una única decisión de configuración.

## ⚙️ Detalles técnicos

```
MvvmLabApp (Application, 1 vez)
  └─ ContenedorApp ── origenDatos = ARCHIVO
        └─ notaRepository (by lazy, UNA instancia) : NotaRepository
              └─ NotasViewModel.Factory ──► NotasViewModel(repositorio)
```

No se usa `AndroidViewModel`: el ViewModel no necesita Context; solo necesitaba el Repository, y ahora lo recibe armado.

## 🔬 Pruebas (emulador)

| Configuración | Resultado |
|---|---|
| `ARCHIVO` | Notas de `notas.json` ✅ |
| `MEMORIA` (una línea) | Sin notas; se agrega una ✅ |
| `MEMORIA` + reiniciar | Vacío (memoria perdida) ✅ |
| `ARCHIVO` de nuevo | Notas del archivo intactas ✅ |

## ❓ Preguntas de la guía

**¿Qué se ganaría al reemplazar el Repository sin tocar el ViewModel?** La lógica del ViewModel queda estable aunque cambie el
almacenamiento (memoria, archivo, Room, red, falso para tests).

**¿Qué instancia debe compartir la app?** Una sola instancia de cada Repository: con varias, en memoria cada una tendría su lista;
con archivo cada una tendría su Mutex y las escrituras podrían pisarse.

## ✅ Criterio de finalización
- ✅ El origen de datos se elige en un punto identificable (`ContenedorApp.origenDatos`) y no en cada recomposición.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej11-dependencias
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
