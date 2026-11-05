package com.example.mvvmlab

// =====================================================================
// MODELO Y ESTADO DE PANTALLA
// =====================================================================

// Una nota YA GUARDADA. No es lo mismo que lo que se esta escribiendo.
data class Nota(
    val titulo: String,
    val contenido: String
)

// "Foto" de todo lo que la pantalla necesita para dibujarse.
data class NotaUiState(
    val titulo: String = "",
    val contenido: String = "",
    // Ej 5: un error POR CAMPO (antes habia uno solo) para mostrarlo
    // cerca del campo que lo provoca. null = sin error.
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val guardando: Boolean = false,
    val guardada: Nota? = null
) {
    val hayCambiosSinGuardar: Boolean
        get() = guardada == null && (titulo.isNotEmpty() || contenido.isNotEmpty()) ||
                guardada != null && (guardada.titulo != titulo || guardada.contenido != contenido)
}
