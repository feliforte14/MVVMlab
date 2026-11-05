package com.example.mvvmlab.presentation.notas

import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.domain.model.Nota

data class NotasUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val notas: List<Nota> = emptyList(),
    // Formulario (sirve para alta y para edicion)
    val editandoId: String? = null,          // null = nota nueva; si no, id de la nota en edicion
    val titulo: String = "",
    val contenido: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val procesando: Boolean = false,         // guardando o eliminando
    val errorOperacion: String? = null       // fallo al guardar/eliminar
) {
    val editando: Boolean get() = editandoId != null
}
