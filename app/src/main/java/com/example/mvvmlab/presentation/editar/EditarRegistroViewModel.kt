package com.example.mvvmlab.presentation.editar

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.core.util.Fechas
import com.example.mvvmlab.di.contenedor
import com.example.mvvmlab.domain.model.Registro
import com.example.mvvmlab.domain.repository.RegistroRepository
import com.example.mvvmlab.domain.validacion.ErroresRegistro
import com.example.mvvmlab.domain.validacion.ValidadorRegistro
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditarRegistroViewModel(
    private val repositorio: RegistroRepository,
    savedStateHandle: SavedStateHandle,
    private val hoy: () -> Long = Fechas::hoy
) : ViewModel() {

    private val id: String? = savedStateHandle.get<String>(ARG_ID)?.takeIf { it != ID_NUEVO }

    private val _uiState = MutableStateFlow(EditarUiState(esNuevo = id == null))
    val uiState: StateFlow<EditarUiState> = _uiState.asStateFlow()

    init {
        if (id == null) {
            val vacio = FormularioRegistro(fecha = hoy())
            _uiState.update { it.copy(carga = EstadoCarga.Listo, formulario = vacio, original = vacio) }
        } else {
            viewModelScope.launch {
                try {
                    val r = repositorio.obtener(id)
                    _uiState.update {
                        if (r == null) it.copy(carga = EstadoCarga.Error("El registro ya no existe"))
                        else {
                            val f = FormularioRegistro(r.titulo, r.descripcion, r.fecha, r.favorito)
                            it.copy(carga = EstadoCarga.Listo, formulario = f, original = f)
                        }
                    }
                } catch (e: IOException) {
                    _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudo leer el registro")) }
                }
            }
        }
    }

    private fun editar(cambio: (FormularioRegistro) -> FormularioRegistro, limpiar: (ErroresRegistro) -> ErroresRegistro) =
        _uiState.update { it.copy(formulario = cambio(it.formulario), errores = limpiar(it.errores), error = null) }

    fun onTituloChange(v: String) = editar({ it.copy(titulo = v) }, { it.copy(titulo = null) })
    fun onDescripcionChange(v: String) = editar({ it.copy(descripcion = v) }, { it.copy(descripcion = null) })
    fun onFechaChange(v: Long) = editar({ it.copy(fecha = v) }, { it.copy(fecha = null) })
    fun onFavoritoChange(v: Boolean) = editar({ it.copy(favorito = v) }, { it })

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando || actual.carga != EstadoCarga.Listo) return
        val f = actual.formulario
        val errores = ValidadorRegistro.validar(f.titulo, f.descripcion, f.fecha, hoy())
        if (errores.hayErrores) {
            _uiState.update { it.copy(errores = errores) }
            return
        }
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repositorio.guardar(
                    Registro(
                        id = id ?: UUID.randomUUID().toString(),
                        titulo = f.titulo.trim(),
                        descripcion = f.descripcion.trim(),
                        fecha = f.fecha,
                        favorito = f.favorito
                    )
                )
                _uiState.update { it.copy(guardando = false, guardadoOk = true) }
            } catch (e: IOException) {
                Log.e("EditarRegistroVM", "No se pudo guardar", e)
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar. Tus datos siguen en pantalla.") }
            }
        }
    }

    companion object {
        const val ARG_ID = "id"
        const val ID_NUEVO = "nuevo"
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { EditarRegistroViewModel(contenedor().registroRepository, createSavedStateHandle()) }
        }
    }
}
