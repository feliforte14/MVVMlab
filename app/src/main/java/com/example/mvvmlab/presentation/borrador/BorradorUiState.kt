package com.example.mvvmlab.presentation.borrador

import com.example.mvvmlab.core.model.EstadoCarga

// Estado de la ultima operacion de GUARDADO.
sealed interface EstadoGuardado {
    data object Inactivo : EstadoGuardado          // todavia no se intento guardar
    data object Guardando : EstadoGuardado
    data object Guardado : EstadoGuardado          // SOLO despues de completar la escritura
    data class Error(val mensaje: String) : EstadoGuardado
}

data class BorradorUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val guardado: EstadoGuardado = EstadoGuardado.Inactivo,
    val texto: String = "",
    val textoGuardado: String = "",
    val existeArchivo: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean get() = texto != textoGuardado
    val puedeEditar: Boolean get() = carga == EstadoCarga.Listo
    val puedeGuardar: Boolean
        get() = puedeEditar && hayCambiosSinGuardar && guardado != EstadoGuardado.Guardando
}
