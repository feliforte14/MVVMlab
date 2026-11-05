package com.example.mvvmlab.presentation.borrador

import androidx.lifecycle.ViewModel

// --- ESTADO DE PANTALLA -----------------------------------------------
data class BorradorUiState(
    val texto: String = "",            // lo que esta en el editor AHORA (estado del ViewModel)
    val textoGuardado: String = "",    // lo que hay en DISCO segun la ultima lectura/escritura
    val guardando: Boolean = false,
    val mensaje: String = "Leyendo borrador...",
    val error: String? = null
) {
    // Ej 8: distinguir visualmente un borrador sin guardar.
    // Calculado: el editor difiere de lo que hay en disco.
    val hayCambiosSinGuardar: Boolean get() = texto != textoGuardado
}
