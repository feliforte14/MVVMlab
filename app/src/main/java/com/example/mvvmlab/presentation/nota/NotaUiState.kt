package com.example.mvvmlab.presentation.nota

import com.example.mvvmlab.domain.model.Nota

// --- ESTADO DE PANTALLA -----------------------------------------------

// "Foto" de todo lo que la pantalla necesita para dibujarse.
// Todos los campos son val (inmutables): para cambiar el estado se crea
// una copia nueva con copy(...). Asi cada cambio es un estado nuevo y
// Compose detecta que tiene que redibujar.
data class NotaUiState(
    val titulo: String = "",          // lo que esta escrito en el campo titulo
    val contenido: String = "",       // lo que esta escrito en el campo contenido
    val error: String? = null,        // mensaje de validacion; null = no hay error
    val guardando: Boolean = false,   // true mientras "se guarda" (operacion en curso)
    val guardada: Nota? = null        // ultima nota guardada; null = todavia ninguna
) {
    // Propiedad calculada: no se guarda, se deduce de los otros campos.
    // Sirve para mostrar "cambios sin guardar" sin duplicar informacion.
    val hayCambiosSinGuardar: Boolean
        get() = guardada == null && (titulo.isNotEmpty() || contenido.isNotEmpty()) ||
                guardada != null && (guardada.titulo != titulo || guardada.contenido != contenido)
}
