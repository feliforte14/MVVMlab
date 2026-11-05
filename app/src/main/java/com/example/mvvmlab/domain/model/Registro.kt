package com.example.mvvmlab.domain.model

// =====================================================================
// EJERCICIO 15 — Cuaderno de campo: MODELO DE DOMINIO Y CONTRATOS
// =====================================================================
// Estos tipos no conocen Room, archivos ni Compose. Las pantallas y los
// ViewModels solo hablan con esto.
// =====================================================================

// Una observacion de una salida, recorrido, visita o actividad.
data class Registro(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fecha: Long,            // dia de la observacion: medianoche UTC en milisegundos
    val favorito: Boolean
)
