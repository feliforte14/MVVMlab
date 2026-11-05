package com.example.mvvmlab.presentation.editar

import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.domain.validacion.ErroresRegistro

// =====================================================================
// EDICION de un registro (alta o modificacion)
// =====================================================================
data class FormularioRegistro(
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0L,
    val favorito: Boolean = false
)

data class EditarUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val esNuevo: Boolean = true,
    val formulario: FormularioRegistro = FormularioRegistro(),
    val original: FormularioRegistro = FormularioRegistro(),
    val errores: ErroresRegistro = ErroresRegistro(),
    val guardando: Boolean = false,
    val error: String? = null,
    val guardadoOk: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean get() = formulario != original
}
