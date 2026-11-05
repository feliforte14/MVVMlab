package com.example.mvvmlab.presentation.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.data.repository.ArchivoBitacoraRepository
import com.example.mvvmlab.di.contenedor
import com.example.mvvmlab.domain.repository.BitacoraRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BitacoraViewModel(
    private val repositorio: BitacoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BitacoraUiState())
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    init { cargar() }

    private fun cargar() {
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        viewModelScope.launch {
            try {
                val texto = repositorio.leer() ?: ""
                _uiState.update { it.copy(carga = EstadoCarga.Listo, texto = texto, textoGuardado = texto, error = null) }
            } catch (e: IOException) {
                _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudo leer la bitácora")) }
            }
        }
    }

    fun onReintentar() = cargar()
    fun onDescartar() = cargar()

    fun onTextoChange(v: String) = _uiState.update { it.copy(texto = v, mensaje = null) }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando || !actual.hayCambiosSinGuardar) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repositorio.guardar(actual.texto)
                _uiState.update {
                    it.copy(guardando = false, textoGuardado = actual.texto, mensaje = "Guardado en ${ArchivoBitacoraRepository.NOMBRE}")
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar la bitácora") }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { BitacoraViewModel(contenedor().bitacoraRepository) }
        }
    }
}
