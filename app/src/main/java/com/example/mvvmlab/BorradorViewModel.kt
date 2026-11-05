package com.example.mvvmlab

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

// --- ESTADO DE PANTALLA -----------------------------------------------
data class BorradorUiState(
    val texto: String = "",
    val mensaje: String = "Leyendo borrador...",   // de donde salio el texto
    val error: String? = null                       // falla real de lectura
)

// --- VIEWMODEL --------------------------------------------------------
class BorradorViewModel(
    private val repositorio: BorradorRepository      // solo conoce el contrato
) : ViewModel() {

    private val _uiState = MutableStateFlow(BorradorUiState())
    val uiState: StateFlow<BorradorUiState> = _uiState.asStateFlow()

    init {
        // El ViewModel SOLICITA la lectura y PUBLICA el resultado.
        // La pantalla nunca toca el sistema de archivos.
        viewModelScope.launch {
            try {
                when (val r = repositorio.leer()) {
                    is ResultadoLectura.Leido -> _uiState.update {
                        it.copy(texto = r.texto, mensaje = "Cargado desde ${ArchivoTextoRepository.NOMBRE_ARCHIVO}")
                    }
                    ResultadoLectura.NoExiste -> _uiState.update {
                        it.copy(mensaje = "Todavía no hay borrador guardado")
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

    companion object {
        private const val TAG = "BorradorViewModel"

        // Fabrica: como el ViewModel ahora RECIBE un repositorio que necesita
        // la carpeta filesDir, Android no puede crearlo solo. Esta fabrica le
        // explica como. Toma el Application (vive lo que vive la app, no una
        // Activity) solo para obtener filesDir.
        // (En el Ej 11 la construccion de dependencias se ordena en un solo lugar.)
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                BorradorViewModel(ArchivoTextoRepository(app.filesDir))
            }
        }
    }
}

// --- PANTALLA CONECTADA AL VIEWMODEL ----------------------------------
@Composable
fun PantallaBorradorConViewModel(viewModel: BorradorViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaBorrador(
        estado = estado,
        onTextoChange = viewModel::onTextoChange,
        modifier = modifier
    )
}
