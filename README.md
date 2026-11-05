# 📓 MvvmLab — Ejercicio 10: De un texto a una lista persistente

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
| 👉 **[`ej10-lista-persistente`](../../tree/ej10-lista-persistente)** | **10 — De un texto a una lista persistente** | JSON, Mutex, escritura segura |
| [`ej11-dependencias`](../../tree/ej11-dependencias) | 11 — Dependencias: ¿quién crea el Repository? | Contenedor en Application |
| [`ej12-dos-pantallas`](../../tree/ej12-dos-pantallas) | 12 — Dos pantallas, una información | Flow como fuente de verdad, navegación |
| [`ej13-tests`](../../tree/ej13-tests) | 13 — Probar el ViewModel sin Android ni archivos | Fake repository, tests de ViewModel |
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador |
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt                    # Obtiene el ViewModel con by viewModels { Factory } y muestra la pantalla
 ├── core/                              # comunes: tema, estados, rutas, utilidades
 │   ├── model/
 │   │   └── EstadoCarga.kt             # Estados sellados de carga: Cargando / Listo / Error
 │   └── theme/
 │       ├── Color.kt                   # Colores del tema
 │       ├── Theme.kt                   # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt                    # Tipografías
 ├── data/                              # CÓMO se guardan los datos
 │   └── repository/
 │       └── ArchivoNotasRepository.kt  # Guarda las notas en notas.json (JSON, Mutex, temporal + renombrado)
 ├── domain/                            # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── Nota.kt                    # Modelo de una nota
 │   ├── repository/
 │   │   └── NotaRepository.kt          # Contrato del Repository de notas
 │   └── validacion/                    # reglas de negocio
 │       └── ValidadorNota.kt           # Reglas: título no vacío, contenido ≥ 10 caracteres
 └── presentation/                      # MVVM: una carpeta por pantalla
     └── notas/
         ├── NotasUiState.kt            # Estado: carga, lista y formulario (alta/edición)
         ├── NotasViewModel.kt          # Alta, edición y borrado a través del Repository; fábrica
         └── PantallaNotas.kt           # Formulario + lista con Editar / Eliminar
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
| Core | `core/model/EstadoCarga.kt` | Estados sellados de carga: `Cargando` / `Listo` / `Error` |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/repository/ArchivoNotasRepository.kt` | Guarda las notas en `notas.json` (JSON, `Mutex`, temporal + renombrado) |
| Domain | `domain/model/Nota.kt` | Modelo de una nota |
| Domain | `domain/repository/NotaRepository.kt` | Contrato del Repository de notas |
| Domain | `domain/validacion/ValidadorNota.kt` | Reglas: título no vacío, contenido ≥ 10 caracteres |
| Presentation | `presentation/notas/NotasUiState.kt` | Estado: carga, lista y formulario (alta/edición) |
| Presentation | `presentation/notas/NotasViewModel.kt` | Alta, edición y borrado a través del Repository; fábrica |
| Presentation | `presentation/notas/PantallaNotas.kt` | Formulario + lista con Editar / Eliminar |

---

<!-- ejercicio -->
## 🎯 Consigna

Extender a varias notas. Elegir un formato de texto claro y documentado (no `split(";")` si el usuario puede escribir
punto y coma). Lectura, alta, edición y borrado a través del Repository.
**Desafío:** escribir primero en un temporal y sustituir el destino sólo cuando la nueva versión esté completa.

## 📋 Formato documentado: `notas.json`

```json
{
  "version": 1,
  "notas": [
    { "id": "24d7e63e-...", "titulo": "Parcial DA1", "contenido": "Linea 1\nLinea 2; con punto y coma" },
    { "id": "507e3e76-...", "titulo": "Cita", "contenido": "Dijo: \"usa JSON\" y ya" }
  ]
}
```

- JSON escapa `\n`, `\"` y `\\`; el `;` no es especial. `org.json` viene con Android (sin dependencias).
- Nota vacía: campos `""`, siempre presentes (nunca omitidos ni `null`).
- Archivo inexistente = lista vacía. Contenido corrupto = `FormatoInvalidoException` (no lista vacía, para no pisar el archivo).
- **Escritura segura:** se escribe `notas.json.tmp` completo y después `renameTo(notas.json)` (atómico).

## 🔬 Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| 3 notas con salto de línea + `;`, comillas, comas | Guardadas; JSON con `\n` y `\"` ✅ |
| Eliminar / Editar | Correcto, la edición conserva el id ✅ |
| `force-stop` y reabrir | Notas intactas ✅ |
| `notas.json` truncado | Error "formato inválido" + Reintentar; archivo no sobrescrito ✅ |
| `ValidadorNotaTest` | 5/5 ✅ |

## ❓ Preguntas de la guía

**¿Cómo se representa una nota vacía?** Con `""` en cada campo, siempre presentes.

**¿Qué hace el lector si el contenido está incompleto?** Lanza `FormatoInvalidoException`; el ViewModel muestra error con
Reintentar y bloquea el formulario. No devuelve lista vacía.

**¿Qué se perdería si se reescribe mal el archivo?** Todas las notas (se reescribe entero). Por eso temporal + renombrado.

## ✅ Criterio de finalización
- ✅ Tres notas con saltos de línea o signos de puntuación sobreviven al reinicio y pueden editarse y eliminarse.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej10-lista-persistente
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
