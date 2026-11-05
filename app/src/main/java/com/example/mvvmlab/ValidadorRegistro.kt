package com.example.mvvmlab

// Reglas de negocio de un registro (Kotlin puro, se prueban sin emulador).
data class ErroresRegistro(
    val titulo: String? = null,
    val descripcion: String? = null,
    val fecha: String? = null
) {
    val hayErrores: Boolean get() = titulo != null || descripcion != null || fecha != null
}

object ValidadorRegistro {
    const val MAX_TITULO = 60

    fun validar(titulo: String, descripcion: String, fecha: Long, hoy: Long): ErroresRegistro =
        ErroresRegistro(
            titulo = when {
                titulo.isBlank() -> "El título no puede estar vacío"
                titulo.trim().length > MAX_TITULO -> "Máximo $MAX_TITULO caracteres"
                else -> null
            },
            descripcion = if (descripcion.isBlank()) "Escribí una descripción" else null,
            fecha = if (fecha > hoy) "La fecha no puede ser futura" else null
        )
}
