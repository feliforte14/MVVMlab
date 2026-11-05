# MvvmLab — Ejercicio 9: Cargando, listo o falló

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
| **`ej09-carga-error`** | **9 — Cargando, listo o falló** ← esta rama |

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
 │   │   ├── ArchivoTextoRepository.kt
 │   │   ├── RepositorioQueFalla.kt
 ├── domain/
 │   ├── model/
 │   │   ├── ResultadoLectura.kt
 │   ├── repository/
 │   │   ├── BorradorRepository.kt
 ├── presentation/
 │   ├── borrador/
 │   │   ├── BorradorUiState.kt
 │   │   ├── BorradorViewModel.kt
 │   │   ├── PantallaBorrador.kt
```

## Consigna

Modelar un estado de carga y uno de error para lectura/escritura. `viewModelScope` para iniciar el trabajo y disco en IO.
Evitar que una excepción cierre la app. Probar la ruta de error con un Repository de prueba que lance una excepción
controlada (sin tocar permisos). **Desafío:** Reintentar si la carga falla.

## Archivos

| Archivo | Cambio |
|---|---|
| `presentation/borrador/BorradorViewModel.kt` | `sealed interface EstadoCarga` (Cargando/Listo/Error) y `EstadoGuardado` (Inactivo/Guardando/Guardado/Error); `try/catch (IOException)`; `onReintentar`; constante `USAR_REPOSITORIO_QUE_FALLA` en la fábrica |
| `data/repository/RepositorioQueFalla.kt` | **Nuevo**: implementación del contrato que falla a propósito (1ª lectura y toda escritura) con demora |
| `presentation/borrador/PantallaBorrador.kt` | `when` exhaustivo: indicador de carga, tarjeta de error con Reintentar, mensaje de guardado |
| `domain/repository/BorradorRepository.kt + data/repository/ArchivoTextoRepository.kt` | Sin cambios (ya despacha en `Dispatchers.IO`) |

## Pruebas (emulador)

| Prueba | Resultado |
|---|---|
| `RepositorioQueFalla`: abrir | "Cargando borrador..." → "No se pudo leer el borrador." + Reintentar, sin cierre ✅ |
| Reintentar | Carga el texto ✅ |
| Editar → Guardar (falla) | "No se pudo guardar. Tus cambios siguen en pantalla." ✅ |
| Repositorio real | Carga y "✓ Guardado" ✅ |

## Preguntas de la guía

**¿Qué excepción no conviene capturar silenciosamente?** `CancellationException`: es cómo se cancelan las corrutinas.
Un `catch (e: Exception)` genérico la atraparía. Por eso se captura solo `IOException`.

**¿Por qué "Guardado" sólo después de completar la escritura?** Si se mostrara antes y la escritura fallara, el usuario
creería que sus datos están a salvo y los perdería.

## Criterio de finalización
- ✅ Se ven estados de carga, éxito y error; ninguna operación de archivo se realiza directamente en la UI.
