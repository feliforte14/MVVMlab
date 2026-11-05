# 📓 MvvmLab

Práctica de **Desarrollo de Aplicaciones I (UADE)** — *Guía de Ejercicios: MVVM y Repository* (v1.0).
Kotlin + Jetpack Compose + Material 3. Una app que va creciendo ejercicio a ejercicio, separando responsabilidades
con **MVVM**, un **Repository** que lee y escribe archivos de texto, **Room**, y un integrador final.

📄 **Apuntes completos** (teoría, resolución paso a paso, pruebas y capturas de los 15 ejercicios):
[`apuntes/Apuntes_MVVM.pdf`](apuntes/Apuntes_MVVM.pdf)

## 🧭 Cómo está organizado

- **`main`**: proyecto base (plantilla *Empty Activity* de Compose, con el tema en `core/theme/`) + apuntes completos.
- **Una rama por ejercicio**, cada una con **solo el código de su ejercicio** y su propio `README.md`.
  Cada rama sale de la anterior, así que la app evoluciona de una a la siguiente.

## 🏛️ Estructura de carpetas

El código se organiza **por capas**, igual que
[PokedexAcademy-MVVM-Clean](https://github.com/ExBattou/PokedexAcademy-MVVM-Clean). Cada carpeta es un paquete de Kotlin
y en cada rama aparecen solo las carpetas que ese ejercicio usa (cada README de rama muestra su árbol completo).

```
app/src/main/java/com/example/mvvmlab/
 ├── MainActivity.kt          # punto de entrada: aloja la UI
 ├── MvvmLabApp.kt            # Application propia (desde el Ej 11)
 ├── core/
 │    ├── model/              # tipos comunes (EstadoCarga)
 │    ├── navigation/         # Rutas de navegación (desde el Ej 12)
 │    ├── theme/              # tema de Compose
 │    └── util/               # utilidades (Fechas)
 ├── data/                    # CÓMO se guardan los datos
 │    ├── local/              # Room: Entity, DAO, Database (Ej 14–15)
 │    ├── mapper/             # Entity ⇄ modelo de dominio
 │    └── repository/         # implementaciones: memoria, archivos, Room
 ├── domain/                  # QUÉ es la app: Kotlin puro, sin Android
 │    ├── model/              # Nota, Registro, ResultadoLectura
 │    ├── repository/         # contratos (interfaces)
 │    └── validacion/         # reglas de negocio
 ├── presentation/            # una carpeta por pantalla: UiState + ViewModel + Pantalla
 └── di/                      # ContenedorApp: quién crea las dependencias (Ej 11)

app/src/test/java/com/example/mvvmlab/
 ├── domain/validacion/       # tests de reglas
 ├── fake/                    # Repository falsos
 ├── presentation/            # tests de ViewModels
 └── util/                    # MainDispatcherRule
```

> A diferencia del repo de referencia, no hay `domain/usecase/`: la guía pide no crear capas "porque sí" y en estos
> ejercicios cada caso de uso solo delegaría en el Repository.

## 🌿 Ramas

| Rama | Ejercicio | Tema principal |
|---|---|---|
| [`ej01-responsabilidades`](../../tree/ej01-responsabilidades) | 1 — ¿Quién está haciendo demasiado? | Detectar responsabilidades mezcladas (View / ViewModel / Repository) |
| [`ej02-estado-pantalla`](../../tree/ej02-estado-pantalla) | 2 — Una pantalla no es su estado | `data class` de estado único, eventos, state hoisting |
| [`ej03-viewmodel`](../../tree/ej03-viewmodel) | 3 — Primer ViewModel, sin magia | `ViewModel`, `by viewModels()`, rotación vs. muerte del proceso |
| [`ej04-stateflow`](../../tree/ej04-stateflow) | 4 — StateFlow: la pantalla observa | `MutableStateFlow` privado, `collectAsStateWithLifecycle` |
| [`ej05-validacion`](../../tree/ej05-validacion) | 5 — Validar sin esconder la regla en un botón | Reglas en Kotlin puro, errores por campo, tests |
| [`ej06-repository`](../../tree/ej06-repository) | 6 — Repository: cambiar datos sin cambiar la pantalla | Contrato `interface`, implementación en memoria |
| [`ej07-leer-archivo`](../../tree/ej07-leer-archivo) | 7 — Leer un archivo de texto desde el Repository | `filesDir`, `Dispatchers.IO`, resultado sellado, fábrica |
| [`ej08-guardar-archivo`](../../tree/ej08-guardar-archivo) | 8 — Guardar texto, cerrar y volver | Escritura UTF-8, cambios sin guardar, descartar |
| [`ej09-carga-error`](../../tree/ej09-carga-error) | 9 — Cargando, listo o falló | Estados sellados, `IOException` vs. cancelación, reintentar |
| [`ej10-lista-persistente`](../../tree/ej10-lista-persistente) | 10 — De un texto a una lista persistente | JSON documentado, `Mutex`, temporal + renombrado |
| [`ej11-dependencias`](../../tree/ej11-dependencias) | 11 — Dependencias: ¿quién crea el Repository? | Contenedor en `Application`, una única decisión de configuración |
| [`ej12-dos-pantallas`](../../tree/ej12-dos-pantallas) | 12 — Dos pantallas, una información | `Flow` como fuente de verdad, navegación con id, `BackHandler` |
| [`ej13-tests`](../../tree/ej13-tests) | 13 — Probar el ViewModel sin Android ni archivos | Fake repository, `MainDispatcherRule`, 14 tests |
| [`ej14-room`](../../tree/ej14-room) | 14 — Room y archivo no son lo mismo | Entity/DAO/Database, adaptador, archivo vs. Room |
| [`ej15-cuaderno-de-campo`](../../tree/ej15-cuaderno-de-campo) | 15 — Integrador: Cuaderno de campo | Room + bitácora.txt + resumen.txt + Intent, 11 tests |

## 📦 Dependencias agregadas durante la práctica

Todas justificadas en los apuntes; no se actualizaron AGP, Kotlin ni Compose.

| Ejercicio | Dependencia | Para qué |
|---|---|---|
| 4 | `lifecycle-runtime-compose 2.10.0` | `collectAsStateWithLifecycle` |
| 12 | `navigation-compose 2.9.0`, `lifecycle-viewmodel-compose 2.10.0` | Navegar entre pantallas; `viewModel()` por destino |
| 13 | `kotlinx-coroutines-test 1.9.0` (solo tests) | `runTest`, `Dispatchers.setMain` |
| 14 | Plugin KSP `2.3.13`, Room `2.8.5` | Base de datos con DAO |

## 🚀 Cómo ejecutar

```bash
git clone https://github.com/feliforte14/MVVMlab.git
cd MVVMlab
git checkout ej15-cuaderno-de-campo     # o la rama del ejercicio que quieras ver
```

1. Abrir la carpeta en **Android Studio** y esperar la sincronización de Gradle.
2. Ejecutar **Run ▶** en un emulador o dispositivo (minSdk 24).
3. Tests (ramas 5 en adelante): `./gradlew :app:testDebugUnitTest`.
