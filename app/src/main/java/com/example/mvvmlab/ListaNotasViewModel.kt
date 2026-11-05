package com.example.mvvmlab

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}

data class ListaUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val notas: List<Nota> = emptyList(),
    val errorOperacion: String? = null
)

// ViewModel de la pantalla LISTA. No guarda su propia copia "de verdad":
// OBSERVA el Flow del Repository y publica lo que llega.
class ListaNotasViewModel(
    private val repositorio: NotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaUiState())
    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    private var observacion: Job? = null

    init {
        observar()
    }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        observacion = viewModelScope.launch {
            try {
                // collect: se queda escuchando. Cada vez que el detalle guarda
                // y el Repository emite, este bloque se vuelve a ejecutar.
                repositorio.observarTodas().collect { notas ->
                    _uiState.update { it.copy(carga = EstadoCarga.Listo, notas = notas) }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la lectura", e)
                _uiState.update { it.copy(carga = EstadoCarga.Error(e.message ?: "No se pudieron leer las notas")) }
            }
        }
    }

    fun onReintentar() = observar()

    fun onEliminar(id: String) {
        viewModelScope.launch {
            try {
                repositorio.eliminar(id)
                // No hace falta recargar: el Flow emite la lista nueva solo.
            } catch (e: IOException) {
                _uiState.update { it.copy(errorOperacion = "No se pudo eliminar la nota") }
            }
        }
    }

    companion object {
        private const val TAG = "ListaNotasViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MvvmLabApp
                ListaNotasViewModel(app.contenedor.notaRepository)
            }
        }
    }
}
