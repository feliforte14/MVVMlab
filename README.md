# MvvmLab — Ejercicio 13: Probar el ViewModel sin Android ni archivos

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
| `ej06-repository` | 6 — Repository: cambiar datos sin cambiar la pantalla |
| `ej07-leer-archivo` | 7 — Leer un archivo de texto desde el Repository |
| `ej08-guardar-archivo` | 8 — Guardar texto, cerrar y volver |
| `ej09-carga-error` | 9 — Cargando, listo o falló |
| `ej10-lista-persistente` | 10 — De un texto a una lista persistente |
| `ej11-dependencias` | 11 — Dependencias: ¿quién crea el Repository? |
| `ej12-dos-pantallas` | 12 — Dos pantallas, una información |
| **`ej13-tests`** | **13 — Probar el ViewModel sin Android ni archivos** ← esta rama |

---

## Estructura de carpetas

Organizado por capas como *PokedexAcademy-MVVM-Clean*: `domain/` (Kotlin puro: modelos, contratos, reglas), `data/` (implementaciones), `presentation/` (ViewModels y pantallas), `di/`, `core/`.

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt
 ├── MvvmLabApp.kt
 ├── core/
 │   ├── model/
 │   │   ├── EstadoCarga.kt
 │   ├── navigation/
 │   │   ├── Rutas.kt
 │   ├── theme/
 │   │   ├── Color.kt
 │   │   ├── Theme.kt
 │   │   ├── Type.kt
 ├── data/
 │   ├── repository/
 │   │   ├── ArchivoNotasRepository.kt
 │   │   ├── NotaRepositoryEnMemoria.kt
 ├── domain/
 │   ├── model/
 │   │   ├── Nota.kt
 │   ├── repository/
 │   │   ├── NotaRepository.kt
 │   ├── validacion/
 │   │   ├── ValidadorNota.kt
 ├── presentation/
 │   ├── AppNotas.kt
 │   ├── detalle/
 │   │   ├── DetalleNotaViewModel.kt
 │   │   ├── DetalleUiState.kt
 │   │   ├── PantallaDetalle.kt
 │   ├── lista/
 │   │   ├── ListaNotasViewModel.kt
 │   │   ├── ListaUiState.kt
 │   │   ├── PantallaLista.kt
 ├── di/
 │   ├── ContenedorApp.kt

app/src/test/java/com/example/mvvmlab/
 ├── domain/
 │   ├── validacion/
 │   │   ├── ValidadorNotaTest.kt
 ├── presentation/
 │   ├── NotasViewModelTest.kt
 ├── fake/
 │   ├── FakeNotaRepository.kt
 ├── util/
 │   ├── MainDispatcherRule.kt
```

## Consigna

Construir un Repository falso para pruebas, con datos prefijados y posibilidad de provocar una falla. Escribir pruebas del
ViewModel para carga inicial, validación, guardado correcto y error de escritura. Justificar los controladores de corrutinas.
**Desafío:** verificar que una solicitud inválida nunca invoca guardar.

## Archivos (en `app/src/test/java/com/example/mvvmlab/`)

| Archivo | Contenido |
|---|---|
| `fake/FakeNotaRepository.kt` | Implementa `NotaRepository`: datos prefijados, `fallarLectura` / `fallarEscritura`, registro `guardadas` / `eliminadas` |
| `util/MainDispatcherRule.kt` | Reemplaza `Dispatchers.Main` por `UnconfinedTestDispatcher` en cada test |
| `presentation/NotasViewModelTest.kt` | 9 tests de `ListaNotasViewModel` y `DetalleNotaViewModel` |
| `domain/validacion/ValidadorNotaTest.kt` | 5 tests del Ej 5 |

Gradle: `testImplementation(kotlinx-coroutines-test 1.9.0)` (misma versión de corrutinas del proyecto) y
`unitTests.isReturnDefaultValues = true` (para que `android.util.Log` no rompa los tests JVM).

**Por qué el controlador de corrutinas:** `viewModelScope` usa `Dispatchers.Main` (hilo de UI de Android), que no existe en la
JVM. La regla lo reemplaza; `UnconfinedTestDispatcher` ejecuta las corrutinas en el momento.

## Resultado: 14/14 ✅

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

## Preguntas de la guía

**¿Qué prueba quedaría innecesariamente difícil si el ViewModel creara su propio Repository?** Todas, en especial las de error:
habría que hacer fallar un archivo real en un emulador. Con el fake alcanza `fallarEscritura = true`.

**¿Qué sí habría que probar en una prueba de integración con archivo real?** Que `ArchivoNotasRepository` escribe y relee el JSON
(tildes, saltos de línea, comillas, `;`), que un archivo truncado da `FormatoInvalidoException`, la escritura con temporal y la
persistencia tras reiniciar.

## Criterio de finalización
- ✅ Hay al menos cuatro pruebas repetibles (14) y ninguna necesita abrir la pantalla.
