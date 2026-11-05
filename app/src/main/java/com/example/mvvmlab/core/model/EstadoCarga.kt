package com.example.mvvmlab.core.model

// --- ESTADO -------------------------------------------------------------

sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}
