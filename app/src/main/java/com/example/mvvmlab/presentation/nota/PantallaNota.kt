package com.example.mvvmlab.presentation.nota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.domain.model.Nota
import kotlinx.coroutines.flow.StateFlow

// =====================================================================
// VIEW (sin estado propio)
// Es la misma del Ej 2: solo DIBUJA el estado que recibe y AVISA los
// eventos con callbacks. No decide nada. Por eso en el Ej 3 se reutiliza
// sin cambios aunque ahora el dueño del estado sea un ViewModel.
// =====================================================================
@Composable
fun PantallaNota(
    estado: NotaUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva nota", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = estado.titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            isError = estado.error != null,
            supportingText = { estado.error?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.contenido,
            onValueChange = onContenidoChange,
            label = { Text("Contenido") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGuardar,
            enabled = !estado.guardando,       // evita doble pulsacion mientras guarda
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.guardando) "Guardando..." else "Guardar")
        }

        if (estado.hayCambiosSinGuardar) {
            Text(
                "Hay cambios sin guardar",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        estado.guardada?.let { nota ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Última nota guardada", style = MaterialTheme.typography.labelMedium)
                    Text(nota.titulo, style = MaterialTheme.typography.titleMedium)
                    if (nota.contenido.isNotBlank()) Text(nota.contenido)
                }
            }
        }
    }
}

// --- PREVIEWS ---------------------------------------------------------
// Como PantallaNota no tiene estado propio, se puede previsualizar en
// cualquier situacion pasandole un NotaUiState armado a mano.

@Preview(showBackground = true, name = "1. Vacía")
@Composable
fun PantallaNotaVaciaPreview() {
    MvvmLabTheme { PantallaNota(NotaUiState(), {}, {}, {}) }
}

@Preview(showBackground = true, name = "2. Error de validación")
@Composable
fun PantallaNotaErrorPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(contenido = "Texto sin título", error = "El título no puede estar vacío"),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true, name = "3. Guardada + cambios sin guardar")
@Composable
fun PantallaNotaGuardadaPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(
                titulo = "Parcial DA1",
                contenido = "Repasar MVVM y ViewModel",
                guardada = Nota("Parcial", "Repasar MVVM")
            ),
            {}, {}, {}
        )
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
