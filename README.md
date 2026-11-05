# 📓 MvvmLab — Ejercicio 9: Cargando, listo o falló

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
| 👉 **[`ej09-carga-error`](../../tree/ej09-carga-error)** | **9 — Cargando, listo o falló** | Estados de carga/error, reintentar |
| [`ej10-lista-persistente`](../../tree/ej10-lista-persistente) | 10 — De un texto a una lista persistente | JSON, Mutex, escritura segura |
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
 │       ├── ArchivoTextoRepository.kt  # Lee y escribe borrador.txt en filesDir (UTF-8, Dispatchers.IO)
 │       └── RepositorioQueFalla.kt     # Implementación de prueba que falla a propósito
 ├── domain/                            # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── ResultadoLectura.kt        # Resultado sellado de leer el archivo: NoExiste / Leido
 │   └── repository/
 │       └── BorradorRepository.kt      # Contrato del Repository del borrador
 └── presentation/                      # MVVM: una carpeta por pantalla
     └── borrador/
         ├── BorradorUiState.kt         # Estado del editor + estados sellados de guardado
         ├── BorradorViewModel.kt       # Pide la lectura/escritura al contrato; fábrica
         └── PantallaBorrador.kt        # Editor sin estado + previews
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
| Data | `data/repository/ArchivoTextoRepository.kt` | Lee y escribe `borrador.txt` en `filesDir` (UTF-8, `Dispatchers.IO`) |
| Data | `data/repository/RepositorioQueFalla.kt` | Implementación de prueba que falla a propósito |
| Domain | `domain/model/ResultadoLectura.kt` | Resultado sellado de leer el archivo: `NoExiste` / `Leido` |
| Domain | `domain/repository/BorradorRepository.kt` | Contrato del Repository del borrador |
| Presentation | `presentation/borrador/BorradorUiState.kt` | Estado del editor + estados sellados de guardado |
| Presentation | `presentation/borrador/BorradorViewModel.kt` | Pide la lectura/escritura al contrato; fábrica |
| Presentation | `presentation/borrador/PantallaBorrador.kt` | Editor sin estado + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Modelar un estado de carga y uno de error para lectura/escritura. `viewModelScope` para iniciar el trabajo y disco en IO.
Evitar que una excepción cierre la app. Probar la ruta de error con un Repository de prueba que lance una excepción
controlada (sin tocar permisos). **Desafío:** Reintentar si la carga falla.

## 🔬 Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| `RepositorioQueFalla`: abrir | "Cargando borrador..." → "No se pudo leer el borrador." + Reintentar, sin cierre ✅ |
| Reintentar | Carga el texto ✅ |
| Editar → Guardar (falla) | "No se pudo guardar. Tus cambios siguen en pantalla." ✅ |
| Repositorio real | Carga y "✓ Guardado" ✅ |

## ❓ Preguntas de la guía

**¿Qué excepción no conviene capturar silenciosamente?** `CancellationException`: es cómo se cancelan las corrutinas.
Un `catch (e: Exception)` genérico la atraparía. Por eso se captura solo `IOException`.

**¿Por qué "Guardado" sólo después de completar la escritura?** Si se mostrara antes y la escritura fallara, el usuario
creería que sus datos están a salvo y los perdería.

## ✅ Criterio de finalización
- ✅ Se ven estados de carga, éxito y error; ninguna operación de archivo se realiza directamente en la UI.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej09-carga-error
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).
