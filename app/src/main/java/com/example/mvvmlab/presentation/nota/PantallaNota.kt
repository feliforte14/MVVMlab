package com.example.mvvmlab.presentation.nota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.domain.model.Nota

// =====================================================================
// VIEW (sin estado propio)
// Ej 6: ademas del formulario muestra la lista de notas guardadas y
// avisa "eliminar" con un callback. No sabe NADA del Repository: recibe
// una List<Nota> ya lista para mostrar.
// =====================================================================
@Composable
fun PantallaNota(
    estado: NotaUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onEliminar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // LazyColumn: lista con scroll que solo dibuja lo visible.
    // El formulario es el primer "item" y despues van las notas.
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Nueva nota", style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = estado.titulo,
                    onValueChange = onTituloChange,
                    label = { Text("Título") },
                    isError = estado.errorTitulo != null,
                    supportingText = { estado.errorTitulo?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.contenido,
                    onValueChange = onContenidoChange,
                    label = { Text("Contenido") },
                    isError = estado.errorContenido != null,
                    supportingText = { estado.errorContenido?.let { Text(it) } },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = onGuardar,
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardando) "Guardando..." else "Guardar")
                }
                Text(
                    "Notas guardadas (${estado.notas.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                if (estado.notas.isEmpty()) {
                    Text("Todavía no hay notas.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // key = id: Compose identifica cada fila por su id (no por posicion),
        // asi al eliminar una nota no se confunden las filas restantes.
        items(estado.notas, key = { it.id }) { nota ->
            FilaNota(nota = nota, onEliminar = { onEliminar(nota.id) })
        }
    }
}

// Composable reutilizable para UNA fila de la lista.
@Composable
fun FilaNota(nota: Nota, onEliminar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(nota.titulo, style = MaterialTheme.typography.titleMedium)
                Text(nota.contenido, style = MaterialTheme.typography.bodyMedium)
            }
            TextButton(onClick = onEliminar) { Text("Eliminar") }
        }
    }
}

// --- PREVIEWS ---------------------------------------------------------

@Preview(showBackground = true, name = "1. Sin notas")
@Composable
fun PantallaNotaVaciaPreview() {
    MvvmLabTheme { PantallaNota(NotaUiState(), {}, {}, {}, {}) }
}

@Preview(showBackground = true, name = "2. Con notas y errores")
@Composable
fun PantallaNotaConNotasPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(
                contenido = "Corto",
                errorTitulo = "El título no puede estar vacío",
                errorContenido = "El contenido debe tener al menos 10 caracteres",
                notas = listOf(
                    Nota("1", "Parcial DA1", "Repasar MVVM y Repository"),
                    Nota("2", "Compras", "Yerba y galletitas")
                )
            ),
            {}, {}, {}, {}
        )
    }
}

// --- PANTALLA CONECTADA AL VIEWMODEL ----------------------------------
@Composable
fun PantallaNotaConViewModel(viewModel: NotaViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaNota(
        estado = estado,
        onTituloChange = viewModel::onTituloChange,
        onContenidoChange = viewModel::onContenidoChange,
        onGuardar = viewModel::onGuardar,
        onEliminar = viewModel::onEliminar,
        modifier = modifier
    )
}
