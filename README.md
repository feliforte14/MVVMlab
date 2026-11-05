# MvvmLab — Ejercicio 10: De un texto a una lista persistente

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
| **`ej10-lista-persistente`** | **10 — De un texto a una lista persistente** ← esta rama |

---

## Estructura de carpetas

Organizado por capas como *PokedexAcademy-MVVM-Clean*: `domain/` (Kotlin puro: modelos, contratos, reglas), `data/` (implementaciones), `presentation/` (ViewModels y pantallas), `di/`, `core/`.

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt
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

app/src/test/java/com/example/mvvmlab/
 ├── domain/
 │   ├── validacion/
 │   │   ├── ValidadorNotaTest.kt
```

## Consigna

Extender a varias notas. Elegir un formato de texto claro y documentado (no `split(";")` si el usuario puede escribir
punto y coma). Lectura, alta, edición y borrado a través del Repository.
**Desafío:** escribir primero en un temporal y sustituir el destino sólo cuando la nueva versión esté completa.

## Archivos

| Archivo | Capa | Contenido |
|---|---|---|
| `domain/repository/NotaRepository.kt` | Datos | `Nota`, contrato `NotaRepository` (el del Ej 6), `FormatoInvalidoException`, `ArchivoNotasRepository` (JSON + `Mutex` + temporal) |
| `presentation/notas/NotasViewModel.kt` | ViewModel | `NotasUiState` (carga, notas, formulario con `editandoId`), alta/edición/borrado, `Factory` |
| `presentation/notas/PantallaNotas.kt` | View | Formulario (nueva / editando) + lista con Editar / Eliminar + estados de carga y error |
| `domain/validacion/ValidadorNota.kt` (+ test) | Reglas | Vuelve del Ej 5 |

## Formato documentado: `notas.json`

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

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| 3 notas con salto de línea + `;`, comillas, comas | Guardadas; JSON con `\n` y `\"` ✅ |
| Eliminar / Editar | Correcto, la edición conserva el id ✅ |
| `force-stop` y reabrir | Notas intactas ✅ |
| `notas.json` truncado | Error "formato inválido" + Reintentar; archivo no sobrescrito ✅ |
| `ValidadorNotaTest` | 5/5 ✅ |

## Preguntas de la guía

**¿Cómo se representa una nota vacía?** Con `""` en cada campo, siempre presentes.

**¿Qué hace el lector si el contenido está incompleto?** Lanza `FormatoInvalidoException`; el ViewModel muestra error con
Reintentar y bloquea el formulario. No devuelve lista vacía.

**¿Qué se perdería si se reescribe mal el archivo?** Todas las notas (se reescribe entero). Por eso temporal + renombrado.

## Criterio de finalización
- ✅ Tres notas con saltos de línea o signos de puntuación sobreviven al reinicio y pueden editarse y eliminarse.
