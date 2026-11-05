package com.example.mvvmlab

// =====================================================================
// MODELO Y ESTADO DE PANTALLA
// (vienen del Ej 2; en el Ej 3 no cambian, solo cambia quien es su dueño)
// =====================================================================

// Una nota YA GUARDADA. No es lo mismo que lo que se esta escribiendo.
data class Nota(
    val titulo: String,
    val contenido: String
)

// "Foto" de todo lo que la pantalla necesita para dibujarse.
// Campos val (inmutables): para cambiar el estado se crea una copia con copy(...).
data class NotaUiState(
    val titulo: String = "",          // lo que esta escrito en el campo titulo
    val contenido: String = "",       // lo que esta escrito en el campo contenido
    val error: String? = null,        // mensaje de validacion; null = no hay error
    val guardando: Boolean = false,   // true mientras "se guarda" (operacion en curso)
    val guardada: Nota? = null        // ultima nota guardada; null = todavia ninguna
) {
    // Propiedad calculada: se deduce de los otros campos, no se duplica.
    val hayCambiosSinGuardar: Boolean
        get() = guardada == null && (titulo.isNotEmpty() || contenido.isNotEmpty()) ||
                guardada != null && (guardada.titulo != titulo || guardada.contenido != contenido)
}
