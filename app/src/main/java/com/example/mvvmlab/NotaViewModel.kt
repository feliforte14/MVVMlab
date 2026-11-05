package com.example.mvvmlab

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
// EJERCICIO 3 — Primer ViewModel, sin magia
// =====================================================================
//
// En el Ej 2 el dueño del estado era un composable (PantallaNotaConEstado)
// con "remember". Problema: remember vive mientras viva la PANTALLA.
// Al rotar el telefono Android DESTRUYE y RECREA la Activity, y con ella
// todo lo que estaba en remember -> se pierde lo escrito.
//
// Solucion: mover el estado y la logica a un ViewModel.
// El ViewModel SOBREVIVE a la rotacion (cambio de configuracion).
//
// Reglas del criterio de finalizacion:
//   - El ViewModel NO llama composables (no dibuja nada).
//   - La Activity NO valida notas (no decide nada).
// =====================================================================

class NotaViewModel : ViewModel() {

    // --- ESTADO ---------------------------------------------------------
    // Mismo NotaUiState del Ej 2. Lo guardamos en un mutableStateOf para
    // que Compose lo observe y redibuje cuando cambia.
    // "private set": desde afuera se puede LEER, pero solo el ViewModel
    // puede CAMBIARLO. La pantalla no puede hacer vm.estado = ...
    // (En el Ej 4 esto se reemplaza por StateFlow.)
    var estado by mutableStateOf(NotaUiState())
        private set

    // Desafio: contador de intentos de guardado, solo para OBSERVAR la vida
    // del ViewModel. Si sobrevive a la rotacion, el numero no vuelve a 0.
    var intentosGuardado by mutableStateOf(0)
        private set

    init {
        // Se ejecuta UNA vez, cuando se crea el ViewModel.
        // Filtrar Logcat por "NotaViewModel" y rotar: NO deberia repetirse.
        Log.d(TAG, "ViewModel CREADO (${hashCode()})")
    }

    // --- EVENTOS --------------------------------------------------------
    // Las mismas reglas que en el Ej 2 eran funciones sueltas
    // (alEditarTitulo, alPulsarGuardar...). Ahora son metodos del ViewModel.

    fun onTituloChange(nuevo: String) {
        estado = estado.copy(titulo = nuevo, error = null)
    }

    fun onContenidoChange(nuevo: String) {
        estado = estado.copy(contenido = nuevo)
    }

    fun onGuardar() {
        if (estado.guardando) return                       // ya se esta guardando: ignorar

        intentosGuardado++                                 // desafio: cuenta TODOS los intentos

        if (estado.titulo.isBlank()) {
            estado = estado.copy(error = "El título no puede estar vacío")
            return
        }

        estado = estado.copy(guardando = true, error = null)

        // viewModelScope: corrutinas atadas a la vida del ViewModel.
        // Si se rota a mitad del guardado, la corrutina SIGUE (el VM no muere).
        // Si el ViewModel se destruye de verdad, se cancelan solas.
        viewModelScope.launch {
            delay(1500)                                    // guardado SIMULADO
            estado = estado.copy(
                guardando = false,
                guardada = Nota(estado.titulo, estado.contenido)
            )
        }
    }

    override fun onCleared() {
        // Se llama cuando el ViewModel se destruye DEFINITIVAMENTE
        // (por ejemplo, al salir de la app con "atras"). NO al rotar.
        Log.d(TAG, "ViewModel DESTRUIDO (${hashCode()})")
    }

    companion object {
        private const val TAG = "NotaViewModel"
    }
}


// --- PANTALLA CONECTADA AL VIEWMODEL ----------------------------------
// Conecta el ViewModel con la View. Reutiliza PantallaNota (la del Ej 2)
// SIN CAMBIOS: como era una View sin estado, no le importa quien es
// el dueño del estado (antes un composable, ahora un ViewModel).
@Composable
fun PantallaNotaConViewModel(viewModel: NotaViewModel, modifier: Modifier = Modifier) {
    // verticalScroll: en horizontal (rotado) la pantalla no entra entera.
    Column(modifier.verticalScroll(rememberScrollState())) {
        PantallaNota(
            estado = viewModel.estado,                     // el estado BAJA
            onTituloChange = viewModel::onTituloChange,    // los eventos SUBEN
            onContenidoChange = viewModel::onContenidoChange,
            onGuardar = viewModel::onGuardar
        )
        Text(
            "Intentos de guardado: ${viewModel.intentosGuardado}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
