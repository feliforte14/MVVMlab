package com.example.mvvmlab.presentation.lista

import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.domain.model.Nota

data class ListaUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val notas: List<Nota> = emptyList(),
    val errorOperacion: String? = null
)
