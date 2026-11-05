package com.example.mvvmlab.domain.model

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
