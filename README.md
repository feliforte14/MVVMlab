# MvvmLab — Ejercicio 7: Leer un archivo de texto desde el Repository

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
| **`ej07-leer-archivo`** | **7 — Leer un archivo de texto desde el Repository** ← esta rama |

---

## Consigna

Crear `ArchivoTextoRepository` para leer `borrador.txt` desde `filesDir`. Si no existe, devolver texto vacío o un resultado
explícito (documentar la decisión). El ViewModel solicita la lectura y publica el resultado. No abrir el archivo desde un
composable. Distinguir archivo inexistente de una falla real; `Dispatchers.IO`; sin referencias a la Activity.

> Los Ej 7–9 de la guía trabajan sobre **un único borrador de texto**: en esta rama la app es un editor de borrador.

## Archivos

| Archivo | Capa | Contenido |
|---|---|---|
| `BorradorRepository.kt` | Datos | `sealed interface ResultadoLectura` (`NoExiste` / `Leido`), `interface BorradorRepository`, `ArchivoTextoRepository(carpeta: File)` |
| `BorradorViewModel.kt` | ViewModel | `BorradorUiState`, `BorradorViewModel` (lee en `init`), `Factory` con `viewModelFactory` |
| `PantallaBorrador.kt` | View | Editor sin estado + 2 previews |
| `MainActivity.kt` | — | `by viewModels { BorradorViewModel.Factory }` |

## Decisión documentada

Si el archivo **no existe** se devuelve el resultado explícito `ResultadoLectura.NoExiste` (no `""`): "no existe" y "existe
pero vacío" son situaciones distintas. Una **falla real** se informa con una excepción (`IOException`), que el ViewModel
captura y muestra como error.

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| Sin `borrador.txt` | "Todavía no hay borrador guardado" ✅ |
| Archivo creado con `adb shell run-as` (3 líneas, tildes, ñ) | Aparece completo al abrir ✅ |
| `borrador.txt` reemplazado por una carpeta | "No se pudo leer el borrador", Logcat `EISDIR`, sin cierre ✅ |

**Desafío — Device Explorer:** `data/data/com.example.mvvmlab/files/borrador.txt`
(o `adb shell run-as com.example.mvvmlab cat files/borrador.txt`).

## Preguntas de la guía

**¿Dónde está físicamente el archivo?** `/data/data/com.example.mvvmlab/files/borrador.txt`, almacenamiento interno privado.

**¿Otra app puede leerlo sin más?** No: cada app tiene su propio usuario Linux y su carpeta privada (sandbox).

## Criterio de finalización
- ✅ Una nota escrita previamente en el archivo aparece al abrir la pantalla.
- ✅ La UI no accede al sistema de archivos.
