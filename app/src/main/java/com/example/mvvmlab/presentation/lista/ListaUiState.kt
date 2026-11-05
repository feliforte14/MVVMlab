package com.example.mvvmlab.presentation.lista

import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.domain.model.Registro

// =====================================================================
// LISTA de registros: busqueda, favoritos, eliminar, resumen
// =====================================================================
data class ListaUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val registros: List<Registro> = emptyList(),
    val busqueda: String = "",
    val mensaje: String? = null,
    val error: String? = null,
    val textoParaCompartir: String? = null      // evento de un solo uso: la UI abre el selector y avisa
)
