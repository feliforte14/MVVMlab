# MvvmLab — Ejercicio 14: Room y archivo no son lo mismo

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
| `ej13-tests` | 13 — Probar el ViewModel sin Android ni archivos |
| **`ej14-room`** | **14 — Room y archivo no son lo mismo** ← esta rama |

---

## Consigna

Crear un adaptador del contrato de notas que use un DAO de Room. Compararlo con el Repository de archivo. Mantener un archivo
de texto para un borrador exportable, sin duplicar toda la base. **Desafío:** tabla con tres decisiones de almacenamiento.

## Archivos

| Archivo | Contenido |
|---|---|
| `RoomNotaRepository.kt` | **Nuevo**: `NotaEntity`, `NotaDao` (Flow, `@Upsert`, búsqueda `LIKE`), `AppDatabase`, `RoomNotaRepository` (adaptador + traducción `SQLiteException` → `IOException`), `aDominio()` / `aEntity()` |
| `ExportadorNotas.kt` | **Nuevo**: `fun interface ExportadorNotas` + `ArchivoExportadorNotas` → `notas_exportadas.txt` |
| `ContenedorApp.kt` | `OrigenDatos.ROOM` (elegido), `AppDatabase` única (`by lazy`), `exportadorNotas` |
| `ListaNotasViewModel.kt`, `Pantallas.kt`, `NavegacionApp.kt` | Botón "Exportar .txt" y mensaje |
| Gradle | Plugin KSP `2.3.13`; Room `2.8.5` (`runtime`, `ktx`, `compiler`) |

`DetalleNotaViewModel`, `PantallaDetalle` y los tests de ViewModels **no cambiaron** (mismo contrato).

## Archivo vs. Room

| | Archivo `notas.json` | Room |
|---|---|---|
| Consultas | Leer todo y filtrar en Kotlin | SQL (`WHERE`, `LIKE`, `ORDER BY`) verificado al compilar |
| Escritura | Reescribe todo (temporal + renombrado) | Por fila, transaccional |
| Errores | `IOException`, JSON inválido, concurrencia a mano | `SQLiteException`, migraciones; SQL inválido no compila |
| Actualizar la UI | `StateFlow` emitido a mano | `Flow` del DAO, automático |

## Tres decisiones de almacenamiento (desafío)

| Dato | Dónde | Por qué |
|---|---|---|
| Notas (registros) | **Room** | Se buscan, ordenan y editan de a uno; la UI se entera de cada cambio; no se corrompen a medias |
| Resumen para compartir | **Archivo .txt** | Texto libre para personas, generado desde la base, nunca se relee: no es fuente de verdad |
| Lo que se está escribiendo | **ViewModel** | Estado de pantalla; se persiste al pulsar Guardar |

## Pruebas

Crear 2 notas ✅ · editar → la lista se actualiza sola y reordena ✅ · Exportar .txt ✅ · reabrir ✅ · tests 14/14 ✅

## Preguntas de la guía

**¿Quién transforma Entity en modelo de dominio?** El adaptador `RoomNotaRepository` (`aDominio()` / `aEntity()`).

**¿Qué gana el ViewModel al no conocer SQL?** No cambia si cambia la base, se prueba con un fake y habla el idioma del problema.

**¿Qué sucede si hay dos fuentes de verdad sin política clara?** Se desincronizan. Política: Room es la única fuente de verdad;
el txt es exportación de solo salida.

## Criterio de finalización
- ✅ La misma pantalla funciona con el contrato de Repository y se explica qué datos van a Room y cuáles a archivo.
