package com.example.mvvmlab.domain.validacion

// =====================================================================
// EJERCICIO 5 — Validar sin esconder la regla en un boton
// =====================================================================
//
// Las REGLAS DE NEGOCIO viven aca: en Kotlin puro, fuera de la vista.
// No conocen Compose ni Android -> se pueden probar con un test comun
// (ver app/src/test/.../ValidadorNotaTest.kt) y valen para cualquier UI.
//
// Reglas:
//   1. El titulo no puede estar vacio (ni ser solo espacios).
//   2. El contenido debe tener al menos 10 caracteres (sin contar los
//      espacios de los extremos).
// (La 3ra regla, "no guardar dos veces mientras se procesa", depende del
//  estado de la operacion y por eso la controla el ViewModel.)
// =====================================================================

// Resultado de validar: un error posible POR CAMPO, para mostrarlo al lado
// del campo correspondiente. null = ese campo esta bien.
data class ErroresNota(
    val titulo: String? = null,
    val contenido: String? = null
) {
    val hayErrores: Boolean get() = titulo != null || contenido != null
}

object ValidadorNota {

    const val MIN_CARACTERES_CONTENIDO = 10

    fun validar(titulo: String, contenido: String): ErroresNota = ErroresNota(
        titulo = if (titulo.isBlank()) "El título no puede estar vacío" else null,
        contenido = if (contenido.trim().length < MIN_CARACTERES_CONTENIDO)
            "El contenido debe tener al menos $MIN_CARACTERES_CONTENIDO caracteres"
        else null
    )
}
