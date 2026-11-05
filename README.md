# 📓 MvvmLab — Ejercicio 7: Leer un archivo de texto desde el Repository

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
| 👉 **[`ej07-leer-archivo`](../../tree/ej07-leer-archivo)** | **7 — Leer un archivo de texto desde el Repository** | filesDir, Dispatchers.IO, resultado sellado |
| [`ej08-guardar-archivo`](../../tree/ej08-guardar-archivo) | 8 — Guardar texto, cerrar y volver | Escritura UTF-8, cambios sin guardar |
| [`ej09-carga-error`](../../tree/ej09-carga-error) | 9 — Cargando, listo o falló | Estados de carga/error, reintentar |
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
 │   └── theme/
 │       ├── Color.kt                   # Colores del tema
 │       ├── Theme.kt                   # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt                    # Tipografías
 ├── data/                              # CÓMO se guardan los datos
 │   └── repository/
 │       └── ArchivoTextoRepository.kt  # Lee borrador.txt de filesDir en Dispatchers.IO
 ├── domain/                            # QUÉ es la app: Kotlin puro, sin Android
 │   ├── model/
 │   │   └── ResultadoLectura.kt        # Resultado sellado de leer el archivo: NoExiste / Leido
 │   └── repository/
 │       └── BorradorRepository.kt      # Contrato del Repository del borrador
 └── presentation/                      # MVVM: una carpeta por pantalla
     └── borrador/
         ├── BorradorUiState.kt         # Estado del editor de borrador
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
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Data | `data/repository/ArchivoTextoRepository.kt` | Lee `borrador.txt` de `filesDir` en `Dispatchers.IO` |
| Domain | `domain/model/ResultadoLectura.kt` | Resultado sellado de leer el archivo: `NoExiste` / `Leido` |
| Domain | `domain/repository/BorradorRepository.kt` | Contrato del Repository del borrador |
| Presentation | `presentation/borrador/BorradorUiState.kt` | Estado del editor de borrador |
| Presentation | `presentation/borrador/BorradorViewModel.kt` | Pide la lectura/escritura al contrato; fábrica |
| Presentation | `presentation/borrador/PantallaBorrador.kt` | Editor sin estado + previews |

---

<!-- ejercicio -->
## 🎯 Consigna

Crear `ArchivoTextoRepository` para leer `borrador.txt` desde `filesDir`. Si no existe, devolver texto vacío o un resultado
explícito (documentar la decisión). El ViewModel solicita la lectura y publica el resultado. No abrir el archivo desde un
composable. Distinguir archivo inexistente de una falla real; `Dispatchers.IO`; sin referencias a la Activity.

> Los Ej 7–9 de la guía trabajan sobre **un único borrador de texto**: en esta rama la app es un editor de borrador.

## 🧠 Decisión documentada

Si el archivo **no existe** se devuelve el resultado explícito `ResultadoLectura.NoExiste` (no `""`): "no existe" y "existe
pero vacío" son situaciones distintas. Una **falla real** se informa con una excepción (`IOException`), que el ViewModel
captura y muestra como error.

## 🔬 Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| Sin `borrador.txt` | "Todavía no hay borrador guardado" ✅ |
| Archivo creado con `adb shell run-as` (3 líneas, tildes, ñ) | Aparece completo al abrir ✅ |
| `borrador.txt` reemplazado por una carpeta | "No se pudo leer el borrador", Logcat `EISDIR`, sin cierre ✅ |

**Desafío — Device Explorer:** `data/data/com.example.mvvmlab/files/borrador.txt`
(o `adb shell run-as com.example.mvvmlab cat files/borrador.txt`).

## ❓ Preguntas de la guía

**¿Dónde está físicamente el archivo?** `/data/data/com.example.mvvmlab/files/borrador.txt`, almacenamiento interno privado.

**¿Otra app puede leerlo sin más?** No: cada app tiene su propio usuario Linux y su carpeta privada (sandbox).

## ✅ Criterio de finalización
- ✅ Una nota escrita previamente en el archivo aparece al abrir la pantalla.
- ✅ La UI no accede al sistema de archivos.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej07-leer-archivo
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).
