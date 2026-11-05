# MvvmLab — Ejercicio 15: Integrador, Cuaderno de campo

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
| `ej14-room` | 14 — Room y archivo no son lo mismo |
| **`ej15-cuaderno-de-campo`** | **15 — Integrador: Cuaderno de campo** ← esta rama |

---

## Consigna

App para guardar observaciones breves de una salida, recorrido, visita o actividad. Cada registro tiene id, título,
descripción, fecha y favorito. Lista, edición, búsqueda por título y manejo claro de estados vacíos o errores.
Registros en **Room** detrás de un contrato; bitácora libre en **`bitacora.txt`**; resumen en **`resumen.txt`**.

## Etapa 1 — Responsabilidades

```
Compose (PantallaLista / PantallaEditar / PantallaBitacora)   ← dibuja y avisa
   │ eventos ↑                              estado ↓ (StateFlow)
ViewModels (ListaRegistrosVM / EditarRegistroVM / BitacoraVM) ← reglas y estados
   │ contratos
RegistroRepository ─► RoomRegistroRepository ─► RegistroDao ─► cuaderno.db
BitacoraRepository ─► ArchivoBitacoraRepository ────────────► bitacora.txt
GeneradorResumen   ─► ArchivoResumen ───────────────────────► resumen.txt
                       (armado por ContenedorApp en MvvmLabApp)
```

| Capa | Archivos |
|---|---|
| View (Compose) | `PantallaLista.kt`, `PantallaEditar.kt`, `PantallaBitacora.kt`, `NavegacionApp.kt` |
| ViewModel | `ViewModels.kt` (Lista, Editar, Bitácora), `ValidadorRegistro.kt` |
| Contratos y modelo | `Modelo.kt` (`Registro`, `RegistroRepository`, `BitacoraRepository`, `GeneradorResumen`, `Fechas`) |
| Room | `RegistroRoom.kt` (Entity, DAO con búsqueda `LIKE`, Database, adaptador) |
| Archivos | `ArchivosCuaderno.kt` (bitácora, resumen, falla controlada) |
| Dependencias | `ContenedorApp.kt` |
| Tests | `app/src/test/.../CuadernoTest.kt` (11), `FakeRegistroRepository.kt`, `MainDispatcherRule.kt` |

## Etapas 2 y 3 — Las dos persistencias

- **Registros → Room** (única fuente de verdad): alta, lectura, edición, baja, favorito; el ViewModel observa el `Flow`
  (búsqueda con `flatMapLatest`), nunca consulta el DAO desde Compose. Orden: favoritos primero, después más recientes.
- **`bitacora.txt`**: texto libre del usuario, UTF-8 (párrafos y tildes), se guarda al tocar Guardar (temporal + renombrado).
- **`resumen.txt`**: generado desde **todos** los registros, **solo a pedido** ("Generar resumen" / "Compartir"). Si falla la
  escritura: error en pantalla, Room intacto y el resumen anterior completo. Nunca se relee.

## Etapa 4 — Experimentos (emulador)

| Experimento | Resultado |
|---|---|
| Cerrar y reabrir | Registros intactos ✅ |
| Rotar mientras se escribe | Título y descripción intactos ✅ |
| Guardar con título vacío | Errores debajo de cada campo, no guarda ✅ |
| Falla controlada (`simularFallaEnRegistros = true`) | "No se pudo marcar como favorito" / "No se pudo guardar. Tus datos siguen en pantalla." ✅ |
| Editar y regresar a la lista | La lista ya muestra el cambio ✅ |
| Verificar archivos y base | `databases/cuaderno.db`, `files/bitacora.txt`, `files/resumen.txt` ✅ |

## Etapa 5 — Tabla de cinco pruebas

| # | Prueba | Resultado observado |
|---|---|---|
| 1 | Alta de 3 registros (1 favorito) → cerrar y reabrir | Persisten; favorito primero ✅ |
| 2 | Buscar "feria" / "feriax" | 1 resultado / "Ningún registro coincide…" ✅ |
| 3 | Bitácora con tildes, ñ y párrafos → Guardar | `bitacora.txt` idéntico ✅ |
| 4 | Generar resumen con búsqueda activa / Compartir | `resumen.txt` con los 3 registros; abre el selector ✅ |
| 5 | `CuadernoTest` (JVM) | 11/11 ✅ |

**Desafío (Intent):** `ACTION_SEND` con `EXTRA_TEXT` = contenido del resumen; no se expone la ruta interna.

**Decisión que cambiaría si el proyecto creciera:** búsqueda con FTS de Room y normalización de tildes (hoy `LIKE` recorre
toda la tabla y "rio" no encuentra "río"); Hilt en vez del contenedor manual; migraciones de Room.

## Preguntas de la guía

**¿Cuál es la fuente de verdad de los registros?** La tabla `registros` de Room, vía `RegistroRepository`.

**¿La bitácora libre y el resumen cumplen el mismo papel?** No: la bitácora es un dato original del usuario; el resumen es
derivado y regenerable desde Room.

**¿Qué sobreviviría a la eliminación del proceso?** Registros, `bitacora.txt` y `resumen.txt` (disco). No: lo escrito sin
guardar, la búsqueda, mensajes y diálogos (memoria).

## Criterio de finalización
- ✅ Flujo completo de registros y bitácora, cerrar y reabrir sin pérdida, y cada parte del código ubicada en View,
  ViewModel, Repository, Room o archivos.
