# MvvmLab — Ejercicio 12: Dos pantallas, una información

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
| **`ej12-dos-pantallas`** | **12 — Dos pantallas, una información** ← esta rama |

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
```

## Consigna

Separar la lista de notas y el detalle/editor. Al guardar desde el detalle, la lista debe mostrar lo actualizado al regresar.
Una fuente de verdad en el Repository; definir cuándo refrescar o emitir cambios. Pasar un identificador por navegación,
no objetos. **Desafío:** volver atrás con cambios sin guardar y decidir qué pregunta la interfaz.

## Archivos

| Archivo | Capa | Contenido |
|---|---|---|
| `domain/repository/NotaRepository.kt` | Datos | Contrato con `observarTodas(): Flow` + `obtener(id)`; `ArchivoNotasRepository` emite tras cada escritura |
| `data/repository/NotaRepositoryEnMemoria.kt` | Datos | Misma API con `MutableStateFlow` |
| `di/ContenedorApp.kt` | App | Sin cambios (una instancia compartida) |
| `presentation/lista/ListaNotasViewModel.kt` | ViewModel | Observa (`collect`) el Flow; eliminar sin recargar |
| `presentation/detalle/DetalleNotaViewModel.kt` | ViewModel | Recibe el `id` por `SavedStateHandle`; carga, valida y guarda |
| `presentation/lista/PantallaLista.kt + presentation/detalle/PantallaDetalle.kt` | View | `PantallaLista` (FAB +) y `PantallaDetalle` (diálogo "¿Descartar cambios?") |
| `presentation/AppNotas.kt` | Navegación | `NavHost`: `"lista"` y `"detalle/{id}"` |
| `libs.versions.toml`, `app/build.gradle.kts` | — | `navigation-compose 2.9.0`, `lifecycle-viewmodel-compose 2.10.0` |

## Decisiones

- **Cuándo emitir:** el Repository actualiza su `StateFlow` después de escribir en disco con éxito → ambas pantallas ven lo mismo.
- **Navegación:** `detalle/{id}` (o `detalle/nueva`). Solo el id; el detalle pide la versión vigente al Repository.
- **Atrás con cambios:** diálogo "¿Descartar cambios?" (Descartar / Seguir editando) vía `BackHandler`; sin cambios, vuelve directo.

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| Editar → atrás | Diálogo; "Seguir editando" conserva el texto ✅ |
| Guardar | Vuelve y la lista muestra lo editado ✅ |
| "+" → nueva → Guardar | Aparece en la lista ✅ |
| `force-stop` y reabrir | Persistido ✅ |

## Preguntas de la guía

**¿Qué ocurre si las dos pantallas guardan una copia distinta de la lista?** Se contradicen o una pisa a la otra. Por eso ambas
observan el Repository.

**¿Qué dato tiene sentido recordar sólo en la navegación?** El id de la nota abierta (y la pila de pantallas), no su contenido.

## Criterio de finalización
- ✅ Después de editar una nota no aparecen versiones contradictorias entre las dos pantallas.
