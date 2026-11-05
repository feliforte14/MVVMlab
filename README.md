# MvvmLab — Ejercicio 8: Guardar texto, cerrar y volver

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
| **`ej08-guardar-archivo`** | **8 — Guardar texto, cerrar y volver** ← esta rama |

---

## Estructura de carpetas

Organizado por capas como *PokedexAcademy-MVVM-Clean*: `domain/` (Kotlin puro: modelos, contratos, reglas), `data/` (implementaciones), `presentation/` (ViewModels y pantallas), `di/`, `core/`.

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt
 ├── core/
 │   ├── theme/
 │   │   ├── Color.kt
 │   │   ├── Theme.kt
 │   │   ├── Type.kt
 ├── data/
 │   ├── repository/
 │   │   ├── ArchivoTextoRepository.kt
 ├── domain/
 │   ├── model/
 │   │   ├── ResultadoLectura.kt
 │   ├── repository/
 │   │   ├── BorradorRepository.kt
 ├── presentation/
 │   ├── borrador/
 │   │   ├── BorradorUiState.kt
 │   │   ├── BorradorViewModel.kt
 │   │   ├── PantallaBorrador.kt
```

## Consigna

Agregar al Repository la escritura del borrador en UTF-8. Escribir varias líneas, guardar, cerrar completamente la app y
volver a abrirla: debe recuperarse el contenido. Documentar si se sobrescribe o se agrega al final.
**Desafío:** botón "Descartar cambios" que vuelva a leer la versión guardada.

## Archivos

| Archivo | Cambio |
|---|---|
| `domain/repository/BorradorRepository.kt + data/repository/ArchivoTextoRepository.kt` | `guardar(texto)` en el contrato; `ArchivoTextoRepository` usa `writeText(texto, UTF_8)` en `Dispatchers.IO` |
| `presentation/borrador/BorradorViewModel.kt` | `textoGuardado` + `hayCambiosSinGuardar`; `onGuardar` y `onDescartarCambios` |
| `presentation/borrador/PantallaBorrador.kt` | Chip "● Sin guardar" / "✓ Guardado", botones Guardar y Descartar cambios |

## Decisión documentada: sobrescribir

Un borrador es "el texto actual", no un historial: `appendText` duplicaría el contenido en cada guardado.
`writeText` crea el archivo si no existe y lo reemplaza.

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| 3 líneas → Guardar → `cat files/borrador.txt` | Las 3 líneas ✅ |
| `am force-stop` y reabrir | Se recupera el contenido ✅ |
| Archivo con "ñandú", "áéíóú" y salto de línea → editar → Guardar | Todo intacto, bytes UTF-8 ✅ |
| Editar → Descartar cambios | Vuelve al texto del archivo ✅ |

## Preguntas de la guía

**¿Qué diferencia hay entre estado del ViewModel y datos en disco?** El estado está en memoria (cambia en cada tecla,
se pierde si muere el proceso); el disco es persistente y solo cambia al guardar. Su diferencia es "cambios sin guardar".

**¿Qué pasa con caracteres acentuados y saltos de línea?** Se conservan si se usa la misma codificación (UTF-8) al
escribir y al leer; con otra codificación aparecerían caracteres raros ("CanciÃ³n").

## Criterio de finalización
- ✅ El texto persiste tras reiniciar y se distingue visualmente un borrador sin guardar.
