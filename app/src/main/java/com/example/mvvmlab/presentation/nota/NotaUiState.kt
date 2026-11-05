package com.example.mvvmlab.presentation.nota

import com.example.mvvmlab.domain.model.Nota

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
