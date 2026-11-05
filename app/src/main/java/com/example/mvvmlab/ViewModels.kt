package com.example.mvvmlab

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

sealed interface EstadoCarga {
    data object Cargando : EstadoCarga
    data object Listo : EstadoCarga
    data class Error(val mensaje: String) : EstadoCarga
}

private fun CreationExtras.contenedor() = (checkNotNull(this[APPLICATION_KEY]) as MvvmLabApp).contenedor

// =====================================================================
// LISTA de registros: busqueda, favoritos, eliminar, resumen
// =====================================================================
data class ListaUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val registros: List<Registro> = emptyList(),
    val busqueda: String = "",
    val mensaje: String? = null,
    val error: String? = null,
    val textoParaCompartir: String? = null      // evento de un solo uso: la UI abre el selector y avisa
)

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

// =====================================================================
// EDICION de un registro (alta o modificacion)
// =====================================================================
data class FormularioRegistro(
    val titulo: String = "",
    val descripcion: String = "",
    val fecha: Long = 0L,
    val favorito: Boolean = false
)

data class EditarUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val esNuevo: Boolean = true,
    val formulario: FormularioRegistro = FormularioRegistro(),
    val original: FormularioRegistro = FormularioRegistro(),
    val errores: ErroresRegistro = ErroresRegistro(),
    val guardando: Boolean = false,
    val error: String? = null,
    val guardadoOk: Boolean = false
) {
    val hayCambiosSinGuardar: Boolean get() = formulario != original
}

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

// =====================================================================
// BITACORA libre (bitacora.txt)
// =====================================================================
data class BitacoraUiState(
    val carga: EstadoCarga = EstadoCarga.Cargando,
    val texto: String = "",
    val textoGuardado: String = "",
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
) {
    val hayCambiosSinGuardar: Boolean get() = texto != textoGuardado
}

class BitacoraViewModel(
    private val repositorio: BitacoraRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BitacoraUiState())
    val uiState: StateFlow<BitacoraUiState> = _uiState.asStateFlow()

    init { cargar() }

    private fun cargar() {
        _uiState.update { it.copy(carga = EstadoCarga.Cargando) }
        viewModelScope.launch {
            try {
                val texto = repositorio.leer() ?: ""
                _uiState.update { it.copy(carga = EstadoCarga.Listo, texto = texto, textoGuardado = texto, error = null) }
            } catch (e: IOException) {
                _uiState.update { it.copy(carga = EstadoCarga.Error("No se pudo leer la bitácora")) }
            }
        }
    }

    fun onReintentar() = cargar()
    fun onDescartar() = cargar()

    fun onTextoChange(v: String) = _uiState.update { it.copy(texto = v, mensaje = null) }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando || !actual.hayCambiosSinGuardar) return
        _uiState.update { it.copy(guardando = true, error = null) }
        viewModelScope.launch {
            try {
                repositorio.guardar(actual.texto)
                _uiState.update {
                    it.copy(guardando = false, textoGuardado = actual.texto, mensaje = "Guardado en ${ArchivoBitacoraRepository.NOMBRE}")
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(guardando = false, error = "No se pudo guardar la bitácora") }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { BitacoraViewModel(contenedor().bitacoraRepository) }
        }
    }
}
