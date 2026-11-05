# 📓 MvvmLab — Ejercicio 1: ¿Quién está haciendo demasiado?

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
| 👉 **[`ej01-responsabilidades`](../../tree/ej01-responsabilidades)** | **1 — ¿Quién está haciendo demasiado?** | Responsabilidades View / ViewModel / Repository |
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
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + archivos + Intent |

---

## 🏛️ Arquitectura de esta rama

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt            # Punto de entrada: muestra la pantalla
 ├── core/                      # comunes: tema, estados, rutas, utilidades
 │   └── theme/
 │       ├── Color.kt           # Colores del tema
 │       ├── Theme.kt           # MvvmLabTheme (Material 3, claro/oscuro)
 │       └── Type.kt            # Tipografías
 └── presentation/              # MVVM: una carpeta por pantalla
     └── tareas/
         └── PantallaTareas.kt  # Pantalla con todo mezclado, para analizar (A–K)
```

```
app/src/test/java/com/example/mvvmlab/
 └── ExampleUnitTest.kt  # Test de ejemplo de la plantilla
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
| Raíz | `MainActivity.kt` | Punto de entrada: muestra la pantalla |
| Core | `core/theme/Color.kt` | Colores del tema |
| Core | `core/theme/Theme.kt` | `MvvmLabTheme` (Material 3, claro/oscuro) |
| Core | `core/theme/Type.kt` | Tipografías |
| Presentation | `presentation/tareas/PantallaTareas.kt` | Pantalla con todo mezclado, para analizar (A–K) |

---

<!-- ejercicio -->
## 🎯 Consigna

Tomar una pantalla donde todo está mezclado, marcar dónde se dibuja la interfaz, dónde se validan los datos,
dónde se modifica la lista y dónde se guarda, y ubicar cada responsabilidad en **View**, **ViewModel** o
**Model/Repository**. Todavía **no** se cambia la implementación.

## 📋 Tabla de responsabilidades (entregable)

| Letra | Código | Qué hace | Dónde va |
|---|---|---|---|
| (A) | `var texto` | Borrador que escribe el usuario; no sobrevive al cierre | ViewModel |
| (B) | `var tareas` | Lista real de tareas; tiene que sobrevivir al cierre | Repository |
| (C) | `var error` | Mensaje de error actual | ViewModel |
| (D) | `Column`, `Text`, `OutlinedTextField` | Dibuja la pantalla | View |
| (E) | `texto.isBlank()` | Regla: no vacía | ViewModel |
| (F) | `tareas.contains(texto)` | Regla: no repetida | ViewModel |
| (G) | `tareas = tareas + texto` | Agrega la tarea | Repository |
| (H) | `File(...).writeText(...)` | Guarda en archivo | Repository |
| (I) | `texto = ""` | Limpia el campo si se agregó bien | ViewModel |
| (J) | `error?.let { Text(it, Color.Red) }` | Muestra el error | View |
| (K) | `LazyColumn` | Muestra la lista | View |

**Criterio para decidir:** ¿dibuja algo? → View · ¿es una regla que vale en cualquier interfaz? → ViewModel ·
¿el dato tiene que sobrevivir si cierro la app? → Repository.
**El ViewModel decide QUÉ mostrar; la View decide CÓMO mostrarlo.**

## 🏆 Desafío: las dos flechas (UDF)

```
            EVENTO (sube)                      operación
  View  ─────────────────────►  ViewModel  ──────────────►  Repository
        "apretó Agregar"                     "agregá esta tarea"

            ESTADO (baja)
  View  ◄─────────────────────  ViewModel
        texto = "", error = null, tareas = [...]
```

## ❓ Preguntas de la guía

**¿Qué trabajo tendría que seguir funcionando aunque mañana cambiemos Compose por otra interfaz?**
Las reglas (E, F) y el manejo de datos (G, H): todo lo que va en ViewModel y Repository.

**¿Qué datos existen sólo mientras se está escribiendo un formulario?**
`texto` (el borrador) y `error` (el mensaje actual): estado de pantalla, no se guardan.

## ✅ Criterio de finalización
- ✅ El esquema identifica 11 responsabilidades y propone dónde ubicar cada una.
<!-- /ejercicio -->

---

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej01-responsabilidades
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).

## 🧪 Tests

```bash
./gradlew :app:testDebugUnitTest
```
O en Android Studio: click derecho sobre `app/src/test` → **Run Tests**. Corren en la JVM, sin emulador.
