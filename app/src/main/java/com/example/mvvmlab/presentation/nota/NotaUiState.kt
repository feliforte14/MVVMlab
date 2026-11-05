package com.example.mvvmlab.presentation.nota

import com.example.mvvmlab.domain.model.Nota

// "Foto" de todo lo que la pantalla necesita para dibujarse.
data class NotaUiState(
    val titulo: String = "",                 // formulario de nueva nota
    val contenido: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val guardando: Boolean = false,
    val notas: List<Nota> = emptyList()      // Ej 6: notas que devolvio el Repository
)
