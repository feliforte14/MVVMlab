package com.example.mvvmlab.presentation.nota

import com.example.mvvmlab.domain.model.Nota

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
