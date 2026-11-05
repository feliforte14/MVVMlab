package com.example.mvvmlab.presentation.notas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.domain.model.Nota

@Composable
fun PantallaNotas(
    estado: NotasUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit,
    onEditar: (Nota) -> Unit,
    onEliminar: (String) -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FormularioNota(estado, onTituloChange, onContenidoChange, onGuardar, onCancelarEdicion)
        }
        item {
            when (val carga = estado.carga) {
                EstadoCarga.Cargando -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                    Text("Cargando notas...")
                }
                is EstadoCarga.Error -> Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(carga.mensaje, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(onClick = onReintentar) { Text("Reintentar") }
                    }
                }
                EstadoCarga.Listo -> Text(
                    if (estado.notas.isEmpty()) "Todavía no hay notas." else "Notas guardadas (${estado.notas.size})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        items(estado.notas, key = { it.id }) { nota ->
            FilaNota(
                nota = nota,
                seleccionada = nota.id == estado.editandoId,
                habilitada = !estado.procesando,
                onEditar = { onEditar(nota) },
                onEliminar = { onEliminar(nota.id) }
            )
        }
    }
}

@Composable
private fun FormularioNota(
    estado: NotasUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (estado.editando) "Editando nota" else "Nueva nota",
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            value = estado.titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            isError = estado.errorTitulo != null,
            supportingText = { estado.errorTitulo?.let { Text(it) } },
            singleLine = true,
            enabled = estado.carga == EstadoCarga.Listo,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.contenido,
            onValueChange = onContenidoChange,
            label = { Text("Contenido") },
            isError = estado.errorContenido != null,
            supportingText = { estado.errorContenido?.let { Text(it) } },
            minLines = 3,
            enabled = estado.carga == EstadoCarga.Listo,
            modifier = Modifier.fillMaxWidth()
        )
        estado.errorOperacion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (estado.editando) {
                OutlinedButton(onClick = onCancelarEdicion, modifier = Modifier.weight(1f)) { Text("Cancelar") }
            }
            Button(
                onClick = onGuardar,
                enabled = !estado.procesando && estado.carga == EstadoCarga.Listo,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when {
                        estado.procesando -> "Guardando..."
                        estado.editando -> "Guardar cambios"
                        else -> "Agregar"
                    }
                )
            }
        }
    }
}

@Composable
fun FilaNota(
    nota: Nota,
    seleccionada: Boolean,
    habilitada: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = if (seleccionada) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(start = 12.dp, top = 10.dp, end = 4.dp, bottom = 4.dp)) {
            Text(nota.titulo, style = MaterialTheme.typography.titleMedium)
            Text(nota.contenido, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onEditar, enabled = habilitada) { Text("Editar") }
                TextButton(onClick = onEliminar, enabled = habilitada) { Text("Eliminar") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaNotasPreview() {
    MvvmLabTheme {
        PantallaNotas(
            NotasUiState(
                carga = EstadoCarga.Listo,
                notas = listOf(
                    Nota("1", "Parcial DA1", "Repasar:\n- MVVM\n- Repository; archivos"),
                    Nota("2", "Cita", "Dijo: \"usá JSON, no split(;)\"")
                ),
                editandoId = "1", titulo = "Parcial DA1", contenido = "Repasar:\n- MVVM"
            ),
            {}, {}, {}, {}, {}, {}, {}
        )
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
