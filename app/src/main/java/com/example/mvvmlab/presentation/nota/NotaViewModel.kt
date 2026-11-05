package com.example.mvvmlab.presentation.nota

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.validacion.ValidadorNota
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotaViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    // --- EVENTOS --------------------------------------------------------

    // Ej 5: ¿cuando limpiar el error? Cuando el usuario EDITA ese campo:
    // muestra que "ya lo esta corrigiendo" sin molestarlo en cada tecla.
    // Solo se limpia el error del campo editado; el del otro se mantiene.
    fun onTituloChange(nuevo: String) {
        _uiState.update { it.copy(titulo = nuevo, errorTitulo = null) }
    }

    fun onContenidoChange(nuevo: String) {
        _uiState.update { it.copy(contenido = nuevo, errorContenido = null) }
    }

    // El boton solo comunica la INTENCION "quiero guardar".
    // Las reglas se aplican aca (y en ValidadorNota), no en la vista.
    fun onGuardar() {
        val actual = _uiState.value

        // Regla 3: no guardar dos veces mientras se procesa una solicitud.
        // (El boton deshabilitado es la ayuda VISUAL; esta linea es la REGLA:
        //  aunque llegaran dos clics seguidos, el segundo se ignora.)
        if (actual.guardando) {
            Log.d(TAG, "onGuardar ignorado: ya se esta guardando")
            return
        }

        // Reglas 1 y 2: delegadas al validador (Kotlin puro).
        val errores = ValidadorNota.validar(actual.titulo, actual.contenido)
        if (errores.hayErrores) {
            _uiState.update {
                it.copy(errorTitulo = errores.titulo, errorContenido = errores.contenido)
            }
            return
        }

        _uiState.update { it.copy(guardando = true) }
        Log.d(TAG, "guardando '${actual.titulo}'")

        viewModelScope.launch {
            delay(1500)                                  // guardado SIMULADO
            _uiState.update {
                it.copy(guardando = false, guardada = Nota(it.titulo, it.contenido))
            }
        }
    }

    companion object {
        private const val TAG = "NotaViewModel"
    }
}
