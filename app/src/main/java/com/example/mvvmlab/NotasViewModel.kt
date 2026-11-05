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
import java.util.UUID

// --- ESTADO -------------------------------------------------------------

sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}

data class NotasUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val notas: List<Nota> = emptyList(),
    // Formulario (sirve para alta y para edicion)
    val editandoId: String? = null,          // null = nota nueva; si no, id de la nota en edicion
    val titulo: String = "",
    val contenido: String = "",
    val errorTitulo: String? = null,
    val errorContenido: String? = null,
    val procesando: Boolean = false,         // guardando o eliminando
    val errorOperacion: String? = null       // fallo al guardar/eliminar
) {
    val editando: Boolean get() = editandoId != null
}

// --- VIEWMODEL ----------------------------------------------------------

class NotasViewModel(
    private val repositorio: NotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotasUiState())
    val uiState: StateFlow<NotasUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    private fun cargar() {
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        viewModelScope.launch {
            try {
                val notas = repositorio.obtenerTodas()
                _uiState.update { it.copy(carga = EstadoCarga.Listo, notas = notas) }
            } catch (e: IOException) {
                Log.e(TAG, "Fallo la lectura", e)
                _uiState.update { it.copy(carga = EstadoCarga.Error(e.message ?: "No se pudieron leer las notas")) }
            }
        }
    }

    fun onReintentar() = cargar()

    fun onTituloChange(nuevo: String) =
        _uiState.update { it.copy(titulo = nuevo, errorTitulo = null) }

    fun onContenidoChange(nuevo: String) =
        _uiState.update { it.copy(contenido = nuevo, errorContenido = null) }

    // Edicion: copia la nota al formulario.
    fun onEditar(nota: Nota) = _uiState.update {
        it.copy(
            editandoId = nota.id, titulo = nota.titulo, contenido = nota.contenido,
            errorTitulo = null, errorContenido = null, errorOperacion = null
        )
    }

    fun onCancelarEdicion() = _uiState.update { limpiarFormulario(it) }

    // Alta o edicion segun editandoId.
    fun onGuardar() {
        val actual = _uiState.value
        if (actual.procesando || actual.carga != EstadoCarga.Listo) return

        val errores = ValidadorNota.validar(actual.titulo, actual.contenido)
        if (errores.hayErrores) {
            _uiState.update { it.copy(errorTitulo = errores.titulo, errorContenido = errores.contenido) }
            return
        }

        val nota = Nota(
            id = actual.editandoId ?: UUID.randomUUID().toString(),
            titulo = actual.titulo.trim(),
            contenido = actual.contenido.trim()
        )
        ejecutar("No se pudo guardar la nota") {
            repositorio.guardar(nota)
            _uiState.update { limpiarFormulario(it) }
        }
    }

    fun onEliminar(id: String) {
        if (_uiState.value.procesando) return
        ejecutar("No se pudo eliminar la nota") {
            repositorio.eliminar(id)
            // Si se estaba editando la nota borrada, se limpia el formulario.
            if (_uiState.value.editandoId == id) _uiState.update { limpiarFormulario(it) }
        }
    }

    // Plantilla comun: marca "procesando", ejecuta la operacion, recarga la
    // lista desde el Repository (fuente de verdad) y maneja el error.
    private fun ejecutar(mensajeError: String, operacion: suspend () -> Unit) {
        _uiState.update { it.copy(procesando = true, errorOperacion = null) }
        viewModelScope.launch {
            try {
                operacion()
                val notas = repositorio.obtenerTodas()
                _uiState.update { it.copy(procesando = false, notas = notas) }
            } catch (e: IOException) {
                Log.e(TAG, mensajeError, e)
                _uiState.update { it.copy(procesando = false, errorOperacion = mensajeError) }
            }
        }
    }

    private fun limpiarFormulario(e: NotasUiState) = e.copy(
        editandoId = null, titulo = "", contenido = "",
        errorTitulo = null, errorContenido = null
    )

    companion object {
        private const val TAG = "NotasViewModel"

        // Ej 11: la fabrica ya NO decide que Repository usar ni lo construye:
        // se lo pide al contenedor de la app. El ViewModel recibe el CONTRATO.
        // (No se usa AndroidViewModel: el ViewModel no necesita Context para
        //  nada; solo necesitaba el Repository, y ahora se lo dan hecho.)
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) as MvvmLabApp
                NotasViewModel(app.contenedor.notaRepository)
            }
        }
    }
}

@Composable
fun PantallaNotasConViewModel(viewModel: NotasViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaNotas(
        estado = estado,
        onTituloChange = viewModel::onTituloChange,
        onContenidoChange = viewModel::onContenidoChange,
        onGuardar = viewModel::onGuardar,
        onCancelarEdicion = viewModel::onCancelarEdicion,
        onEditar = viewModel::onEditar,
        onEliminar = viewModel::onEliminar,
        onReintentar = viewModel::onReintentar,
        modifier = modifier
    )
}
