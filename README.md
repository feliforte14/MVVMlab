# 📓 MvvmLab — Ejercicio 13: Probar el ViewModel sin Android ni archivos

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
| [`ej11-dependencias`](../../tree/ej11-dependencias) | 11 — Dependencias: ¿quién crea el Repository? | Contenedor en Application |
| [`ej12-dos-pantallas`](../../tree/ej12-dos-pantallas) | 12 — Dos pantallas, una información | Flow como fuente de verdad, navegación |
| 👉 **[`ej13-tests`](../../tree/ej13-tests)** | **13 — Probar el ViewModel sin Android ni archivos** | Fake repository, tests de ViewModel |
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador |
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt                     # Punto de entrada: aloja la navegación
 ├── MvvmLabApp.kt                       # Application propia: crea el ContenedorApp una sola vez
 ├── core/                               # comunes: tema, estados, rutas, utilidades
 │   ├── model/
 │   │   └── EstadoCarga.kt              # Estados sellados de carga: Cargando / Listo / Error
 │   ├── navigation/
 │   │   └── Rutas.kt                    # Nombres de las rutas de navegación y su argumento id
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
 │   ├── AppNotas.kt                     # NavHost: lista ⇄ detalle/{id}
 │   ├── detalle/
 │   │   ├── DetalleNotaViewModel.kt     # Recibe el id por SavedStateHandle; valida y guarda
 │   │   ├── DetalleUiState.kt           # Estado del editor + cambios sin guardar
 │   │   └── PantallaDetalle.kt          # Editor con diálogo “¿Descartar cambios?”
 │   └── lista/
 │       ├── ListaNotasViewModel.kt      # Observa el Flow del Repository; eliminar
 │       ├── ListaUiState.kt             # Estado de la lista
 │       └── PantallaLista.kt            # Lista de notas con botón + y Eliminar
 └── di/                                 # quién crea las dependencias
     └── ContenedorApp.kt                # Crea una instancia de cada Repository (única decisión de configuración)
```

```
app/src/test/java/com/example/mvvmlab/
 ├── domain/                       # tests de reglas
 │   └── validacion/
 │       └── ValidadorNotaTest.kt  # 5 tests de las reglas
 ├── presentation/                 # tests de ViewModels
 │   └── NotasViewModelTest.kt     # 9 tests de los ViewModels (carga, validación, guardado, error)
 ├── fake/                         # dobles de prueba (Repository falsos)
 │   └── FakeNotaRepository.kt     # Repository falso: datos fijos, fallas a pedido, registro de llamadas
 └── util/                         # utilidades de test
     └── MainDispatcherRule.kt     # Reemplaza Dispatchers.Main en los tests
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
| Raíz | `MainActivity.kt` | Punto de entrada: aloja la navegación |
| Raíz | `MvvmLabApp.kt` | `Application` propia: crea el `ContenedorApp` una sola vez |
| Core | `core/model/EstadoCarga.kt` | Estados sellados de carga: `Cargando` / `Listo` / `Error` |
| Core | `core/navigation/Rutas.kt` | Nombres de las rutas de navegación y su argumento `id` |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/repository/ArchivoNotasRepository.kt` | Guarda las notas en `notas.json` (JSON, `Mutex`, temporal + renombrado) |
| Data | `data/repository/NotaRepositoryEnMemoria.kt` | Implementación en memoria (se pierde al cerrar la app) |
| Domain | `domain/model/Nota.kt` | Modelo de una nota |
| Domain | `domain/repository/NotaRepository.kt` | Contrato del Repository de notas |
| Domain | `domain/validacion/ValidadorNota.kt` | Reglas: título no vacío, contenido ≥ 10 caracteres |
| Presentation | `presentation/AppNotas.kt` | `NavHost`: lista ⇄ detalle/{id} |
| Presentation | `presentation/detalle/DetalleNotaViewModel.kt` | Recibe el id por `SavedStateHandle`; valida y guarda |
| Presentation | `presentation/detalle/DetalleUiState.kt` | Estado del editor + cambios sin guardar |
| Presentation | `presentation/detalle/PantallaDetalle.kt` | Editor con diálogo “¿Descartar cambios?” |
| Presentation | `presentation/lista/ListaNotasViewModel.kt` | Observa el `Flow` del Repository; eliminar |
| Presentation | `presentation/lista/ListaUiState.kt` | Estado de la lista |
| Presentation | `presentation/lista/PantallaLista.kt` | Lista de notas con botón + y Eliminar |
| DI | `di/ContenedorApp.kt` | Crea una instancia de cada Repository (única decisión de configuración) |

---

<!-- ejercicio -->
## 🎯 Consigna

Construir un Repository falso para pruebas, con datos prefijados y posibilidad de provocar una falla. Escribir pruebas del
ViewModel para carga inicial, validación, guardado correcto y error de escritura. Justificar los controladores de corrutinas.
**Desafío:** verificar que una solicitud inválida nunca invoca guardar.

## ⚙️ Detalles técnicos

Gradle: `testImplementation(kotlinx-coroutines-test 1.9.0)` (misma versión de corrutinas del proyecto) y
`unitTests.isReturnDefaultValues = true` (para que `android.util.Log` no rompa los tests JVM).

**Por qué el controlador de corrutinas:** `viewModelScope` usa `Dispatchers.Main` (hilo de UI de Android), que no existe en la
JVM. La regla lo reemplaza; `UnconfinedTestDispatcher` ejecuta las corrutinas en el momento.

## 🔬 Resultado: 14/14 ✅

| Área | Tests |
|---|---|
| Carga inicial | `lista_cargaInicial_...`, `lista_siLaLecturaFalla_...`, `detalle_conId_...` |
| Validación | `detalle_tituloVacio_...`, **`detalle_solicitudInvalida_nuncaInvocaGuardar`** (desafío) |
| Guardado correcto | `detalle_notaValida_...`, `guardarEnElDetalle_actualizaLaLista_...` |
| Error de escritura | `detalle_siFallaLaEscritura_...`, `lista_siFallaEliminar_...` |
| Reglas (Ej 5) | 5 tests de `ValidadorNotaTest` |

**Prueba de mutación:** con la validación desactivada a propósito fallan 2 tests (título vacío y "nunca invoca guardar");
restaurado el código, pasan los 14.

```bash
./gradlew :app:testDebugUnitTest
```

## ❓ Preguntas de la guía

**¿Qué prueba quedaría innecesariamente difícil si el ViewModel creara su propio Repository?** Todas, en especial las de error:
habría que hacer fallar un archivo real en un emulador. Con el fake alcanza `fallarEscritura = true`.

**¿Qué sí habría que probar en una prueba de integración con archivo real?** Que `ArchivoNotasRepository` escribe y relee el JSON
(tildes, saltos de línea, comillas, `;`), que un archivo truncado da `FormatoInvalidoException`, la escritura con temporal y la
persistencia tras reiniciar.

## ✅ Criterio de finalización
- ✅ Hay al menos cuatro pruebas repetibles (14) y ninguna necesita abrir la pantalla.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej13-tests
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
