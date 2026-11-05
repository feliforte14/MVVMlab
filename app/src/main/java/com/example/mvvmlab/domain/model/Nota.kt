package com.example.mvvmlab.domain.model

// =====================================================================
// EJERCICIO 10 — De un texto a una lista persistente
// =====================================================================

// Modelo: una nota guardada. id identifica la nota para editarla o borrarla.
data class Nota(
    val id: String,
    val titulo: String,
    val contenido: String
)
