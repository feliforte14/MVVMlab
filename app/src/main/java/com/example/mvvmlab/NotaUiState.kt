package com.example.mvvmlab

// =====================================================================
// MODELO Y ESTADO DE PANTALLA
// =====================================================================

// Una nota guardada. Ej 6: ahora tiene un id, porque hay VARIAS notas y
// el Repository necesita saber cual eliminar o reemplazar.
data class Nota(
    val id: String,
    val titulo: String,
    val contenido: String
)

// "Foto" de todo lo que la pantalla necesita para dibujarse.
data class NotaUiState(
    val titulo: String = "",                 // formulario de nueva nota
    val contenido: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val guardando: Boolean = false,
    val notas: List<Nota> = emptyList()      // Ej 6: notas que devolvio el Repository
)
