package com.example.mvvmlab

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

data class DetalleUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val esNueva: Boolean = true,
    val titulo: String = "",
    val contenido: String = "",
    // Version guardada (para detectar cambios sin guardar).
    val tituloOriginal: String = "",
    val contenidoOriginal: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val guardando: Boolean = false,
    val errorOperacion: String? = null,
    val guardadoOk: Boolean = false          // la pantalla reacciona volviendo a la lista
) {
    val hayCambiosSinGuardar: Boolean
        get() = titulo != tituloOriginal || contenido != contenidoOriginal
}

// ViewModel de la pantalla DETALLE/EDITOR.
// Recibe por navegacion SOLO el id de la nota (no el objeto entero) y
// le pide la nota al Repository: asi siempre edita la version vigente.
class DetalleNotaViewModel(
    private val repositorio: NotaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // SavedStateHandle contiene los argumentos de navegacion ("detalle/{id}").
    private val id: String? = savedStateHandle.get<String>(ARG_ID)?.takeIf { it != ID_NUEVA }

    private val _uiState = MutableStateFlow(DetalleUiState(esNueva = id == null))
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        if (id == null) {
            _uiState.update { it.copy(carga = EstadoCarga.Listo) }
        } else {
            viewModelScope.launch {
                try {
                    val nota = repositorio.obtener(id)
                    _uiState.update {
                        if (nota == null) it.copy(carga = EstadoCarga.Error("La nota ya no existe"))
                        else it.copy(
                            carga = EstadoCarga.Listo,
                            titulo = nota.titulo, contenido = nota.contenido,
                            tituloOriginal = nota.titulo, contenidoOriginal = nota.contenido
                        )
                    }
                } catch (e: IOException) {
                    _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudo leer la nota")) }
                }
            }
        }
    }

    fun onTituloChange(nuevo: String) = _uiState.update { it.copy(titulo = nuevo, errorTitulo = null) }

    fun onContenidoChange(nuevo: String) = _uiState.update { it.copy(contenido = nuevo, errorContenido = null) }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando || actual.carga != EstadoCarga.Listo) return
        val errores = ValidadorNota.validar(actual.titulo, actual.contenido)
        if (errores.hayErrores) {
            _uiState.update { it.copy(errorTitulo = errores.titulo, errorContenido = errores.contenido) }
            return
        }
        _uiState.update { it.copy(guardando = true, errorOperacion = null) }
        viewModelScope.launch {
            try {
                repositorio.guardar(
                    Nota(id ?: UUID.randomUUID().toString(), actual.titulo.trim(), actual.contenido.trim())
                )
                _uiState.update { it.copy(guardando = false, guardadoOk = true) }
            } catch (e: IOException) {
                Log.e("DetalleNotaViewModel", "No se pudo guardar", e)
                _uiState.update { it.copy(guardando = false, errorOperacion = "No se pudo guardar la nota") }
            }
        }
    }

    companion object {
        const val ARG_ID = "id"
        const val ID_NUEVA = "nueva"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MvvmLabApp
                DetalleNotaViewModel(app.contenedor.notaRepository, createSavedStateHandle())
            }
        }
    }
}
