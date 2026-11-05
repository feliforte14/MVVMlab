package com.example.mvvmlab.presentation.borrador

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.data.repository.ArchivoTextoRepository
import com.example.mvvmlab.data.repository.RepositorioQueFalla
import com.example.mvvmlab.domain.model.ResultadoLectura
import com.example.mvvmlab.domain.repository.BorradorRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BorradorViewModel(
    private val repositorio: BorradorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BorradorUiState())
    val uiState: StateFlow<BorradorUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        // viewModelScope inicia el trabajo; el Repository se encarga de
        // pasarlo a Dispatchers.IO (withContext) para el acceso a disco.
        viewModelScope.launch {
            try {
                val r = repositorio.leer()
                val texto = (r as? ResultadoLectura.Leido)?.texto ?: ""
                _uiState.update {
                    it.copy(
                        carga = EstadoCarga.Listo,
                        texto = texto,
                        textoGuardado = texto,
                        existeArchivo = r is ResultadoLectura.Leido,
                        guardado = EstadoGuardado.Inactivo
                    )
                }
            } catch (e: IOException) {
                // Se captura SOLO IOException (fallas de disco esperables).
                // NO se usa "catch (e: Exception)": eso tambien atraparia la
                // CancellationException con la que las corrutinas se cancelan
                // (por ejemplo al destruirse el ViewModel) y la "silenciaria".
                Log.e(TAG, "Fallo la lectura", e)
                _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudo leer el borrador.")) }
            }
        }
    }

    // Desafio: ofrecer Reintentar si la carga falla.
    fun onReintentar() = cargar()

    fun onTextoChange(nuevo: String) {
        _uiState.update {
            // Si estaba en "Guardado" o "Error", al editar vuelve a Inactivo:
            // el mensaje ya no describe el texto actual.
            it.copy(texto = nuevo, guardado = EstadoGuardado.Inactivo)
        }
    }

    fun onGuardar() {
        val actual = _uiState.value
        if (!actual.puedeGuardar) return
        _uiState.update { it.copy(guardado = EstadoGuardado.Guardando) }
        viewModelScope.launch {
            try {
                repositorio.guardar(actual.texto)
                // "Guardado" recien cuando la escritura TERMINO sin error.
                _uiState.update {
                    it.copy(guardado = EstadoGuardado.Guardado, textoGuardado = actual.texto, existeArchivo = true)
                }
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la escritura", e)
                _uiState.update { it.copy(guardado = EstadoGuardado.Error("No se pudo guardar. Tus cambios siguen en pantalla.")) }
            }
        }
    }

    fun onDescartarCambios() = cargar()

    companion object {
        private const val TAG = "BorradorViewModel"

        // Cambiar a true para probar la ruta de error con RepositorioQueFalla.
        const val USAR_REPOSITORIO_QUE_FALLA = false

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val repositorio: BorradorRepository =
                    if (USAR_REPOSITORIO_QUE_FALLA) RepositorioQueFalla()
                    else ArchivoTextoRepository(checkNotNull(this[APPLICATION_KEY]).filesDir)
                BorradorViewModel(repositorio)
            }
        }
    }
}
