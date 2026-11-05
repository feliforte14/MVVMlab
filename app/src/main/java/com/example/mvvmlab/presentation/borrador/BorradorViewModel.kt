package com.example.mvvmlab.presentation.borrador

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmlab.data.repository.ArchivoTextoRepository
import com.example.mvvmlab.domain.model.ResultadoLectura
import com.example.mvvmlab.domain.repository.BorradorRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// --- VIEWMODEL --------------------------------------------------------
class BorradorViewModel(
    private val repositorio: BorradorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BorradorUiState())
    val uiState: StateFlow<BorradorUiState> = _uiState.asStateFlow()

    init {
        cargarDesdeDisco()
    }

    // Lee el archivo y reemplaza el texto del editor por lo que hay en disco.
    // Se usa al abrir y en "Descartar cambios" (desafio).
    private fun cargarDesdeDisco() {
        viewModelScope.launch {
            try {
                when (val r = repositorio.leer()) {
                    is ResultadoLectura.Leido -> _uiState.update {
                        it.copy(
                            texto = r.texto, textoGuardado = r.texto, error = null,
                            mensaje = "Cargado desde ${ArchivoTextoRepository.NOMBRE_ARCHIVO}"
                        )
                    }
                    ResultadoLectura.NoExiste -> _uiState.update {
                        it.copy(texto = "", textoGuardado = "", error = null,
                            mensaje = "Todavía no hay borrador guardado")
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "No se pudo leer el borrador", e)
                _uiState.update { it.copy(mensaje = "", error = "No se pudo leer el borrador") }
            }
        }
    }

    fun onTextoChange(nuevo: String) {
        _uiState.update { it.copy(texto = nuevo) }
    }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repositorio.guardar(actual.texto)
                // Recien DESPUES de escribir se actualiza lo "guardado".
                _uiState.update {
                    it.copy(guardando = false, textoGuardado = actual.texto,
                        mensaje = "Guardado en ${ArchivoTextoRepository.NOMBRE_ARCHIVO}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "No se pudo guardar el borrador", e)
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar el borrador") }
            }
        }
    }

    // Desafio: volver a la version guardada (relee el archivo).
    fun onDescartarCambios() {
        cargarDesdeDisco()
    }

    companion object {
        private const val TAG = "BorradorViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                BorradorViewModel(ArchivoTextoRepository(app.filesDir))
            }
        }
    }
}
