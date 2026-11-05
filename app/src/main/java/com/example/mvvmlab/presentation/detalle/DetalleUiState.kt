package com.example.mvvmlab.presentation.detalle

import com.example.mvvmlab.core.model.EstadoCarga

data class DetalleUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val esNueva: Boolean = true,
    val titulo: String = "",
    val contenido: String = "",
    // Version guardada (para detectar cambios sin guardar).
    val tituloOriginal: String = "",
    val contenidoOriginal: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val guardando: Boolean = false,
    val errorOperacion: String? = null,
    val guardadoOk: Boolean = false          // la pantalla reacciona volviendo a la lista
) {
    val hayCambiosSinGuardar: Boolean
        get() = titulo != tituloOriginal || contenido != contenidoOriginal
}
