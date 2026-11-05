# 📓 MvvmLab — Ejercicio 14: Room y archivo no son lo mismo

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
| [`ej13-tests`](../../tree/ej13-tests) | 13 — Probar el ViewModel sin Android ni archivos | Fake repository, tests de ViewModel |
| 👉 **[`ej14-room`](../../tree/ej14-room)** | **14 — Room y archivo no son lo mismo** | Entity/DAO/Database, adaptador |
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
 │   ├── local/                          # Room: Entity, DAO, Database
 │   │   ├── AppDatabase.kt              # Base de datos Room (notas.db)
 │   │   ├── NotaDao.kt                  # Consultas SQL: Flow, @Upsert, búsqueda LIKE
 │   │   └── NotaEntity.kt               # Tabla notas
 │   ├── mapper/                         # Entity ⇄ dominio
 │   │   └── NotaMapper.kt               # NotaEntity ⇄ Nota
 │   └── repository/
 │       ├── ArchivoExportadorNotas.kt   # Genera notas_exportadas.txt (resumen legible)
 │       ├── ArchivoNotasRepository.kt   # Guarda las notas en notas.json (JSON, Mutex, temporal + renombrado)
 │       ├── NotaRepositoryEnMemoria.kt  # Implementación en memoria (se pierde al cerrar la app)
 │       └── RoomNotaRepository.kt       # Adaptador Room del contrato; traduce SQLiteException → IOException
 ├── domain/                             # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── Nota.kt                     # Modelo de una nota
 │   ├── repository/
 │   │   ├── ExportadorNotas.kt          # Contrato para exportar las notas a texto
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
 │       ├── ListaNotasViewModel.kt      # Observa el Flow del Repository; eliminar y exportar a .txt
 │       ├── ListaUiState.kt             # Estado de la lista
 │       └── PantallaLista.kt            # Lista de notas con +, Eliminar y “Exportar .txt”
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
| Data | `data/local/AppDatabase.kt` | Base de datos Room (`notas.db`) |
| Data | `data/local/NotaDao.kt` | Consultas SQL: `Flow`, `@Upsert`, búsqueda `LIKE` |
| Data | `data/local/NotaEntity.kt` | Tabla `notas` |
| Data | `data/mapper/NotaMapper.kt` | `NotaEntity` ⇄ `Nota` |
| Data | `data/repository/ArchivoExportadorNotas.kt` | Genera `notas_exportadas.txt` (resumen legible) |
| Data | `data/repository/ArchivoNotasRepository.kt` | Guarda las notas en `notas.json` (JSON, `Mutex`, temporal + renombrado) |
| Data | `data/repository/NotaRepositoryEnMemoria.kt` | Implementación en memoria (se pierde al cerrar la app) |
| Data | `data/repository/RoomNotaRepository.kt` | Adaptador Room del contrato; traduce `SQLiteException` → `IOException` |
| Domain | `domain/model/Nota.kt` | Modelo de una nota |
| Domain | `domain/repository/ExportadorNotas.kt` | Contrato para exportar las notas a texto |
| Domain | `domain/repository/NotaRepository.kt` | Contrato del Repository de notas |
| Domain | `domain/validacion/ValidadorNota.kt` | Reglas: título no vacío, contenido ≥ 10 caracteres |
| Presentation | `presentation/AppNotas.kt` | `NavHost`: lista ⇄ detalle/{id} |
| Presentation | `presentation/detalle/DetalleNotaViewModel.kt` | Recibe el id por `SavedStateHandle`; valida y guarda |
| Presentation | `presentation/detalle/DetalleUiState.kt` | Estado del editor + cambios sin guardar |
| Presentation | `presentation/detalle/PantallaDetalle.kt` | Editor con diálogo “¿Descartar cambios?” |
| Presentation | `presentation/lista/ListaNotasViewModel.kt` | Observa el `Flow` del Repository; eliminar y exportar a .txt |
| Presentation | `presentation/lista/ListaUiState.kt` | Estado de la lista |
| Presentation | `presentation/lista/PantallaLista.kt` | Lista de notas con +, Eliminar y “Exportar .txt” |
| DI | `di/ContenedorApp.kt` | Crea una instancia de cada Repository (única decisión de configuración) |

---

<!-- ejercicio -->
## 🎯 Consigna

Crear un adaptador del contrato de notas que use un DAO de Room. Compararlo con el Repository de archivo. Mantener un archivo
de texto para un borrador exportable, sin duplicar toda la base. **Desafío:** tabla con tres decisiones de almacenamiento.

## ⚙️ Detalles técnicos

`DetalleNotaViewModel`, `PantallaDetalle` y los tests de ViewModels **no cambiaron** (mismo contrato).

## 🧠 Archivo vs. Room

## 🧠 Tres decisiones de almacenamiento (desafío)

| Dato | Dónde | Por qué |
|---|---|---|
| Notas (registros) | **Room** | Se buscan, ordenan y editan de a uno; la UI se entera de cada cambio; no se corrompen a medias |
| Resumen para compartir | **Archivo .txt** | Texto libre para personas, generado desde la base, nunca se relee: no es fuente de verdad |
| Lo que se está escribiendo | **ViewModel** | Estado de pantalla; se persiste al pulsar Guardar |

## 🔬 Pruebas

Crear 2 notas ✅ · editar → la lista se actualiza sola y reordena ✅ · Exportar .txt ✅ · reabrir ✅ · tests 14/14 ✅

## ❓ Preguntas de la guía

**¿Quién transforma Entity en modelo de dominio?** El adaptador `RoomNotaRepository` (`aDominio()` / `aEntity()`).

**¿Qué gana el ViewModel al no conocer SQL?** No cambia si cambia la base, se prueba con un fake y habla el idioma del problema.

**¿Qué sucede si hay dos fuentes de verdad sin política clara?** Se desincronizan. Política: Room es la única fuente de verdad;
el txt es exportación de solo salida.

## ✅ Criterio de finalización
- ✅ La misma pantalla funciona con el contrato de Repository y se explica qué datos van a Room y cuáles a archivo.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej14-room
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
