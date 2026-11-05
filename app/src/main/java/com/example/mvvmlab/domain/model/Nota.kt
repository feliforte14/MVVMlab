package com.example.mvvmlab.domain.model

// =====================================================================
// MODELO Y ESTADO DE PANTALLA
// (vienen del Ej 2; en el Ej 3 no cambian, solo cambia quien es su dueño)
// =====================================================================

// Una nota YA GUARDADA. No es lo mismo que lo que se esta escribiendo.
data class Nota(
    val titulo: String,
    val contenido: String
)
