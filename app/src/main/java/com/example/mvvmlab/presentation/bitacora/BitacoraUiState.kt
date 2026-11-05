package com.example.mvvmlab.presentation.bitacora

import com.example.mvvmlab.core.model.EstadoCarga

// =====================================================================
// BITACORA libre (bitacora.txt)
// =====================================================================
data class BitacoraUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val texto: String = "",
    val textoGuardado: String = "",
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
) {
    val hayCambiosSinGuardar: Boolean get() = texto != textoGuardado
}
