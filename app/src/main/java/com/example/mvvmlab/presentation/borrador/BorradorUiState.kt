package com.example.mvvmlab.presentation.borrador

// --- ESTADO DE PANTALLA -----------------------------------------------
data class BorradorUiState(
    val texto: String = "",
    val mensaje: String = "Leyendo borrador...",   // de donde salio el texto
    val error: String? = null                       // falla real de lectura
)
