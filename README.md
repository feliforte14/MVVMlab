# 📓 MvvmLab — Ejercicio 15: Integrador, Cuaderno de campo

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
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador |
| 👉 **[`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo)** | **15 — Integrador: Cuaderno de campo** | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt                               # Punto de entrada: aloja la navegación
 ├── MvvmLabApp.kt                                 # Application propia: crea el ContenedorApp una sola vez
 ├── core/                                         # comunes: tema, estados, rutas, utilidades
 │   ├── model/
 │   │   └── EstadoCarga.kt                        # Estados sellados de carga: Cargando / Listo / Error
 │   ├── navigation/
 │   │   └── Rutas.kt                              # Nombres de las rutas de navegación y su argumento id
 │   ├── theme/
 │   │   ├── Color.kt                              # Colores del tema
 │   │   ├── Theme.kt                              # MvvmLabTheme (Material 3, claro/oscuro)
 │   │   └── Type.kt                               # Tipografías
 │   └── util/
 │       └── Fechas.kt                             # Fecha de hoy (UTC) y formato dd/MM/yyyy
 ├── data/                                         # CÓMO se guardan los datos
 │   ├── local/                                    # Room: Entity, DAO, Database
 │   │   ├── CuadernoDatabase.kt                   # Base de datos Room (cuaderno.db)
 │   │   ├── RegistroDao.kt                        # Consultas: búsqueda por título, orden por favorito y fecha
 │   │   └── RegistroEntity.kt                     # Tabla registros
 │   ├── mapper/                                   # Entity ⇄ dominio
 │   │   └── RegistroMapper.kt                     # RegistroEntity ⇄ Registro
 │   └── repository/
 │       ├── ArchivoBitacoraRepository.kt          # bitacora.txt: texto libre en UTF-8
 │       ├── ArchivoResumen.kt                     # Genera resumen.txt a pedido
 │       ├── FallaControladaRegistroRepository.kt  # Hace fallar las escrituras (experimento de la Etapa 4)
 │       └── RoomRegistroRepository.kt             # Adaptador Room del contrato de registros
 ├── domain/                                       # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── Registro.kt                           # Modelo: id, título, descripción, fecha, favorito
 │   ├── repository/
 │   │   ├── BitacoraRepository.kt                 # Contrato de la bitácora
 │   │   ├── GeneradorResumen.kt                   # Contrato para generar el resumen
 │   │   └── RegistroRepository.kt                 # Contrato de registros (Flow + búsqueda)
 │   └── validacion/                               # reglas de negocio
 │       └── ValidadorRegistro.kt                  # Reglas: título (≤ 60), descripción, fecha no futura
 ├── presentation/                                 # MVVM: una carpeta por pantalla
 │   ├── AppCuaderno.kt                            # NavHost: lista, editar/{id} y bitácora
 │   ├── bitacora/
 │   │   ├── BitacoraUiState.kt                    # Estado de la bitácora
 │   │   ├── BitacoraViewModel.kt                  # Lee y guarda bitacora.txt a través del contrato
 │   │   └── PantallaBitacora.kt                   # Editor de texto libre
 │   ├── editar/
 │   │   ├── EditarRegistroViewModel.kt            # Carga por id, valida y guarda
 │   │   ├── EditarUiState.kt                      # Formulario, versión original y errores por campo
 │   │   └── PantallaEditar.kt                     # Formulario con DatePicker y diálogo de descarte
 │   └── lista/
 │       ├── ListaRegistrosViewModel.kt            # Búsqueda (flatMapLatest), favoritos, eliminar, resumen
 │       ├── ListaUiState.kt                       # Estado: registros, búsqueda, mensajes, texto a compartir
 │       └── PantallaLista.kt                      # Lista, búsqueda, estados vacíos y compartir (Intent)
 └── di/                                           # quién crea las dependencias
     └── ContenedorApp.kt                          # Crea una instancia de cada Repository (única decisión de configuración)
```

```
app/src/test/java/com/example/mvvmlab/
 ├── presentation/                  # tests de ViewModels
 │   └── CuadernoTest.kt            # 11 tests del integrador (reglas y ViewModels)
 ├── fake/                          # dobles de prueba (Repository falsos)
 │   └── FakeRegistroRepository.kt  # Repository falso de registros
 └── util/                          # utilidades de test
     └── MainDispatcherRule.kt      # Reemplaza Dispatchers.Main en los tests
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
| Core | `core/util/Fechas.kt` | Fecha de hoy (UTC) y formato `dd/MM/yyyy` |
| Data | `data/local/CuadernoDatabase.kt` | Base de datos Room (`cuaderno.db`) |
| Data | `data/local/RegistroDao.kt` | Consultas: búsqueda por título, orden por favorito y fecha |
| Data | `data/local/RegistroEntity.kt` | Tabla `registros` |
| Data | `data/mapper/RegistroMapper.kt` | `RegistroEntity` ⇄ `Registro` |
| Data | `data/repository/ArchivoBitacoraRepository.kt` | `bitacora.txt`: texto libre en UTF-8 |
| Data | `data/repository/ArchivoResumen.kt` | Genera `resumen.txt` a pedido |
| Data | `data/repository/FallaControladaRegistroRepository.kt` | Hace fallar las escrituras (experimento de la Etapa 4) |
| Data | `data/repository/RoomRegistroRepository.kt` | Adaptador Room del contrato de registros |
| Domain | `domain/model/Registro.kt` | Modelo: id, título, descripción, fecha, favorito |
| Domain | `domain/repository/BitacoraRepository.kt` | Contrato de la bitácora |
| Domain | `domain/repository/GeneradorResumen.kt` | Contrato para generar el resumen |
| Domain | `domain/repository/RegistroRepository.kt` | Contrato de registros (`Flow` + búsqueda) |
| Domain | `domain/validacion/ValidadorRegistro.kt` | Reglas: título (≤ 60), descripción, fecha no futura |
| Presentation | `presentation/AppCuaderno.kt` | `NavHost`: lista, editar/{id} y bitácora |
| Presentation | `presentation/bitacora/BitacoraUiState.kt` | Estado de la bitácora |
| Presentation | `presentation/bitacora/BitacoraViewModel.kt` | Lee y guarda `bitacora.txt` a través del contrato |
| Presentation | `presentation/bitacora/PantallaBitacora.kt` | Editor de texto libre |
| Presentation | `presentation/editar/EditarRegistroViewModel.kt` | Carga por id, valida y guarda |
| Presentation | `presentation/editar/EditarUiState.kt` | Formulario, versión original y errores por campo |
| Presentation | `presentation/editar/PantallaEditar.kt` | Formulario con `DatePicker` y diálogo de descarte |
| Presentation | `presentation/lista/ListaRegistrosViewModel.kt` | Búsqueda (`flatMapLatest`), favoritos, eliminar, resumen |
| Presentation | `presentation/lista/ListaUiState.kt` | Estado: registros, búsqueda, mensajes, texto a compartir |
| Presentation | `presentation/lista/PantallaLista.kt` | Lista, búsqueda, estados vacíos y compartir (Intent) |
| DI | `di/ContenedorApp.kt` | Crea una instancia de cada Repository (única decisión de configuración) |

---

<!-- ejercicio -->
## 🎯 Consigna

App para guardar observaciones breves de una salida, recorrido, visita o actividad. Cada registro tiene id, título,
descripción, fecha y favorito. Lista, edición, búsqueda por título y manejo claro de estados vacíos o errores.
Registros en **Room** detrás de un contrato; bitácora libre en **`bitacora.txt`**; resumen en **`resumen.txt`**.

## 🧩 Etapa 1 — Responsabilidades

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

## 🧩 Etapas 2 y 3 — Las dos persistencias

- **Registros → Room** (única fuente de verdad): alta, lectura, edición, baja, favorito; el ViewModel observa el `Flow`
  (búsqueda con `flatMapLatest`), nunca consulta el DAO desde Compose. Orden: favoritos primero, después más recientes.
- **`bitacora.txt`**: texto libre del usuario, UTF-8 (párrafos y tildes), se guarda al tocar Guardar (temporal + renombrado).
- **`resumen.txt`**: generado desde **todos** los registros, **solo a pedido** ("Generar resumen" / "Compartir"). Si falla la
  escritura: error en pantalla, Room intacto y el resumen anterior completo. Nunca se relee.

## 🧩 Etapa 4 — Experimentos (emulador)

| Experimento | Resultado |
|---|---|
| Cerrar y reabrir | Registros intactos ✅ |
| Rotar mientras se escribe | Título y descripción intactos ✅ |
| Guardar con título vacío | Errores debajo de cada campo, no guarda ✅ |
| Falla controlada (`simularFallaEnRegistros = true`) | "No se pudo marcar como favorito" / "No se pudo guardar. Tus datos siguen en pantalla." ✅ |
| Editar y regresar a la lista | La lista ya muestra el cambio ✅ |
| Verificar archivos y base | `databases/cuaderno.db`, `files/bitacora.txt`, `files/resumen.txt` ✅ |

## 🧩 Etapa 5 — Tabla de cinco pruebas

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

## ❓ Preguntas de la guía

**¿Cuál es la fuente de verdad de los registros?** La tabla `registros` de Room, vía `RegistroRepository`.

**¿La bitácora libre y el resumen cumplen el mismo papel?** No: la bitácora es un dato original del usuario; el resumen es
derivado y regenerable desde Room.

**¿Qué sobreviviría a la eliminación del proceso?** Registros, `bitacora.txt` y `resumen.txt` (disco). No: lo escrito sin
guardar, la búsqueda, mensajes y diálogos (memoria).

## ✅ Criterio de finalización
- ✅ Flujo completo de registros y bitácora, cerrar y reabrir sin pérdida, y cada parte del código ubicada en View,
  ViewModel, Repository, Room o archivos.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej15-cuaderno-de-campo
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
