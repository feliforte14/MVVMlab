package com.example.mvvmlab

import android.util.Log
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// =====================================================================
// EJERCICIO 4 — StateFlow: la pantalla observa
// =====================================================================
//
// En el Ej 3 el estado era un mutableStateOf (API de Compose) con
// "private set". Ahora el ViewModel expone un StateFlow (API de Kotlin,
// no de Compose): un "contenedor observable" que siempre tiene un valor
// actual y avisa a quien lo observe cada vez que cambia.
//
// Patron "backing property":
//   _uiState  -> MutableStateFlow, PRIVADO: solo el ViewModel escribe.
//   uiState   -> StateFlow, PUBLICO y de SOLO LECTURA: la pantalla observa.
// =====================================================================

class NotaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    // --- EVENTOS --------------------------------------------------------
    // update { actual -> nuevo } toma el valor ACTUAL y lo reemplaza por el
    // que devuelve la lambda. Como cada evento hace copy() solo del campo
    // que le corresponde, editar el titulo nunca borra el contenido
    // (desafio de la guia).

    fun onTituloChange(nuevo: String) {
        _uiState.update { it.copy(titulo = nuevo, error = null) }
        registrar("titulo")
    }

    fun onContenidoChange(nuevo: String) {
        _uiState.update { it.copy(contenido = nuevo) }
        registrar("contenido")
    }

    fun onGuardar() {
        val actual = _uiState.value                      // .value = valor actual del flujo
        if (actual.guardando) return

        if (actual.titulo.isBlank()) {
            _uiState.update { it.copy(error = "El título no puede estar vacío") }
            registrar("guardar (error)")
            return
        }

        _uiState.update { it.copy(guardando = true, error = null) }
        registrar("guardar (inicio)")

        viewModelScope.launch {
            delay(1500)                                  // guardado SIMULADO
            _uiState.update {
                it.copy(guardando = false, guardada = Nota(it.titulo, it.contenido))
            }
            registrar("guardar (fin)")
        }
    }

    // Desafio: "registrar cambios puntuales". Filtrar Logcat por NotaViewModel.
    private fun registrar(evento: String) {
        val e = _uiState.value
        Log.d(TAG, "$evento -> titulo='${e.titulo}' contenido='${e.contenido}' " +
                "error=${e.error} guardando=${e.guardando}")
    }

    companion object {
        private const val TAG = "NotaViewModel"
    }
}


// --- PANTALLA CONECTADA AL VIEWMODEL ----------------------------------
@Composable
fun PantallaNotaConViewModel(viewModel: NotaViewModel, modifier: Modifier = Modifier) {
    // collectAsStateWithLifecycle: se suscribe al StateFlow y convierte cada
    // valor nuevo en un State de Compose -> cuando cambia, se recompone.
    // "WithLifecycle": deja de recolectar cuando la app pasa a segundo plano
    // (no gasta trabajo dibujando algo que nadie ve) y retoma al volver.
    //
    // Compose NO guarda una segunda copia de la nota: lee siempre la del VM.
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    PantallaNota(
        estado = estado,                                 // el estado BAJA
        onTituloChange = viewModel::onTituloChange,      // los eventos SUBEN
        onContenidoChange = viewModel::onContenidoChange,
        onGuardar = viewModel::onGuardar,
        modifier = modifier.verticalScroll(rememberScrollState())
    )
}
