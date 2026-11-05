# MvvmLab — Ejercicio 11: Dependencias, ¿quién crea el Repository?

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
| **`ej11-dependencias`** | **11 — Dependencias: ¿quién crea el Repository?** ← esta rama |

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
 │   ├── notas/
 │   │   ├── NotasUiState.kt
 │   │   ├── NotasViewModel.kt
 │   │   ├── PantallaNotas.kt
 ├── di/
 │   ├── ContenedorApp.kt

app/src/test/java/com/example/mvvmlab/
 ├── domain/
 │   ├── validacion/
 │   │   ├── ValidadorNotaTest.kt
```

## Consigna

Revisar dónde se construyen el Repository y el ViewModel. Suministrar la dependencia desde la capa de aplicación
(una fábrica explícita alcanza). El ViewModel recibe el contrato, no crea archivos con un Context de la UI.
Justificar si se usa `AndroidViewModel`.
**Desafío:** alternar entre Repository en memoria y de archivos con una única decisión de configuración.

## Archivos

| Archivo | Cambio |
|---|---|
| `di/ContenedorApp.kt` | **Nuevo**: `enum OrigenDatos`, `ContenedorApp` (única decisión `origenDatos` + `notaRepository by lazy`) y `MvvmLabApp : Application` |
| `data/repository/NotaRepositoryEnMemoria.kt` | **Nuevo**: implementación en memoria del contrato (con `Mutex`) |
| `presentation/notas/NotasViewModel.kt` | La `Factory` pide `app.contenedor.notaRepository` en vez de construirlo |
| `AndroidManifest.xml` | `android:name=".MvvmLabApp"` |

```
MvvmLabApp (Application, 1 vez)
  └─ ContenedorApp ── origenDatos = ARCHIVO
        └─ notaRepository (by lazy, UNA instancia) : NotaRepository
              └─ NotasViewModel.Factory ──► NotasViewModel(repositorio)
```

No se usa `AndroidViewModel`: el ViewModel no necesita Context; solo necesitaba el Repository, y ahora lo recibe armado.

## Pruebas (emulador)

| Configuración | Resultado |
|---|---|
| `ARCHIVO` | Notas de `notas.json` ✅ |
| `MEMORIA` (una línea) | Sin notas; se agrega una ✅ |
| `MEMORIA` + reiniciar | Vacío (memoria perdida) ✅ |
| `ARCHIVO` de nuevo | Notas del archivo intactas ✅ |

## Preguntas de la guía

**¿Qué se ganaría al reemplazar el Repository sin tocar el ViewModel?** La lógica del ViewModel queda estable aunque cambie el
almacenamiento (memoria, archivo, Room, red, falso para tests).

**¿Qué instancia debe compartir la app?** Una sola instancia de cada Repository: con varias, en memoria cada una tendría su lista;
con archivo cada una tendría su Mutex y las escrituras podrían pisarse.

## Criterio de finalización
- ✅ El origen de datos se elige en un punto identificable (`ContenedorApp.origenDatos`) y no en cada recomposición.
