package com.example.mvvmlab.presentation.lista

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.data.repository.ArchivoResumen
import com.example.mvvmlab.di.contenedor
import com.example.mvvmlab.domain.model.Registro
import com.example.mvvmlab.domain.repository.GeneradorResumen
import com.example.mvvmlab.domain.repository.RegistroRepository
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ListaRegistrosViewModel(
    private val repositorio: RegistroRepository,
    private val generadorResumen: GeneradorResumen
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaUiState())
    val uiState: StateFlow<ListaUiState> = _uiState.asStateFlow()

    private val busqueda = MutableStateFlow("")
    private var observacion: Job? = null

    init { observar() }

    private fun observar() {
        observacion?.cancel()
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        observacion = viewModelScope.launch {
            try {
                // flatMapLatest: cada vez que cambia el texto de busqueda, deja
                // de observar la consulta anterior y observa la nueva.
                busqueda.flatMapLatest { repositorio.observar(it) }.collect { registros ->
                    _uiState.update { it.copy(carga = EstadoCarga.Listo, registros = registros) }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la lectura", e)
                _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudieron leer los registros")) }
            }
        }
    }

    fun onReintentar() = observar()

    fun onBusquedaChange(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        busqueda.value = texto
    }

    fun onFavorito(registro: Registro) = ejecutar("No se pudo marcar como favorito") {
        repositorio.cambiarFavorito(registro.id, !registro.favorito)
    }

    fun onEliminar(id: String) = ejecutar("No se pudo eliminar el registro") {
        repositorio.eliminar(id)
    }

    // Genera resumen.txt con TODOS los registros (no solo los filtrados).
    fun onGenerarResumen(compartir: Boolean) = ejecutar("No se pudo generar el resumen") {
        val todos = repositorio.observar("").first()
        val texto = generadorResumen.generar(todos)
        _uiState.update {
            it.copy(
                mensaje = "Resumen generado (${todos.size} registros) en ${ArchivoResumen.NOMBRE}",
                textoParaCompartir = if (compartir) texto else null
            )
        }
    }

    fun onCompartido() = _uiState.update { it.copy(textoParaCompartir = null) }

    fun onMensajeVisto() = _uiState.update { it.copy(mensaje = null, error = null) }

    private fun ejecutar(mensajeError: String, operacion: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(error = null) }
                operacion()
            } catch (e: IOException) {
                Log.e(TAG, mensajeError, e)
                _uiState.update { it.copy(error = mensajeError) }
            }
        }
    }

    companion object {
        private const val TAG = "ListaRegistrosVM"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                ListaRegistrosViewModel(c.registroRepository, c.generadorResumen)
            }
        }
    }
}
