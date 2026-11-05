package com.example.mvvmlab.presentation.nota

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvmlab.data.repository.NotaRepositoryEnMemoria
import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.NotaRepository
import com.example.mvvmlab.domain.validacion.ValidadorNota
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Ej 6: el ViewModel recibe el Repository por constructor y SOLO conoce
// el contrato (NotaRepository), no la implementacion concreta.
//
// El valor por defecto es el unico lugar donde se elige la implementacion.
// Desafio: cambiar NotaRepositoryEnMemoria() por NotaRepositoryDeEjemplo()
// muestra notas precargadas sin modificar ningun composable.
// (Como todos los parametros tienen valor por defecto, Kotlin genera un
//  constructor sin parametros y "by viewModels()" lo puede crear.
//  En el Ej 11 esto se reemplaza por una fabrica explicita.)
class NotaViewModel(
    private val repositorio: NotaRepository = NotaRepositoryEnMemoria()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotaUiState())
    val uiState: StateFlow<NotaUiState> = _uiState.asStateFlow()

    init {
        // Al crearse, pide las notas al Repository.
        viewModelScope.launch { recargarNotas() }
    }

    // Pide la "foto" actual al Repository y la publica en el estado.
    private suspend fun recargarNotas() {
        val notas = repositorio.obtenerTodas()
        _uiState.update { it.copy(notas = notas) }
    }

    // --- EVENTOS --------------------------------------------------------

    fun onTituloChange(nuevo: String) {
        _uiState.update { it.copy(titulo = nuevo, errorTitulo = null) }
    }

    fun onContenidoChange(nuevo: String) {
        _uiState.update { it.copy(contenido = nuevo, errorContenido = null) }
    }

    fun onGuardar() {
        val actual = _uiState.value
        if (actual.guardando) return

        val errores = ValidadorNota.validar(actual.titulo, actual.contenido)
        if (errores.hayErrores) {
            _uiState.update {
                it.copy(errorTitulo = errores.titulo, errorContenido = errores.contenido)
            }
            return
        }

        _uiState.update { it.copy(guardando = true) }
        viewModelScope.launch {
            val nota = Nota(
                id = UUID.randomUUID().toString(),   // identificador unico
                titulo = actual.titulo.trim(),
                contenido = actual.contenido.trim()
            )
            repositorio.guardar(nota)                // el VM pide, el Repository guarda
            recargarNotas()
            _uiState.update { it.copy(guardando = false, titulo = "", contenido = "") }
        }
    }

    fun onEliminar(id: String) {
        viewModelScope.launch {
            repositorio.eliminar(id)
            recargarNotas()
        }
    }
}
