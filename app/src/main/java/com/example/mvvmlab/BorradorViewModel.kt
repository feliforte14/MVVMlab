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

// =====================================================================
// EJERCICIO 9 — Cargando, listo o fallo
// =====================================================================
// En vez de banderas sueltas (cargando=true, error!=null, ...) que se
// pueden combinar de forma ambigua, cada operacion tiene un tipo SELLADO
// con sus estados posibles. Un "when" sobre ellos es exhaustivo.
// =====================================================================

// Estado de la LECTURA inicial del borrador.
sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}

// Estado de la ultima operacion de GUARDADO.
sealed interface EstadoGuardado {
    data object Inactivo : EstadoGuardado          // todavia no se intento guardar
    data object Guardando : EstadoGuardado
    data object Guardado : EstadoGuardado          // SOLO despues de completar la escritura
    data class Error(val mensaje: String) : EstadoGuardado
}

data class BorradorUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val guardado: EstadoGuardado = EstadoGuardado.Inactivo,
    val texto: String = "",
    val textoGuardado: String = "",
    val existeArchivo: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean get() = texto != textoGuardado
    val puedeEditar: Boolean get() = carga == EstadoCarga.Listo
    val puedeGuardar: Boolean
        get() = puedeEditar && hayCambiosSinGuardar && guardado != EstadoGuardado.Guardando
}

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

@Composable
fun PantallaBorradorConViewModel(viewModel: BorradorViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaBorrador(
        estado = estado,
        onTextoChange = viewModel::onTextoChange,
        onGuardar = viewModel::onGuardar,
        onDescartarCambios = viewModel::onDescartarCambios,
        onReintentar = viewModel::onReintentar,
        modifier = modifier
    )
}
