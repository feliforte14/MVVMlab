package com.example.mvvmlab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.ui.theme.MvvmLabTheme

// =====================================================================
// PANTALLA 1 — LISTA
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    estado: ListaUiState,
    onAbrir: (String) -> Unit,          // navegar al detalle con el ID
    onNueva: () -> Unit,
    onEliminar: (String) -> Unit,
    onReintentar: () -> Unit,
    onExportar: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis notas") },
                actions = { TextButton(onClick = onExportar) { Text("Exportar .txt") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNueva) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                when (val c = estado.carga) {
                    EstadoCarga.Cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                        Text("  Cargando notas...")
                    }
                    is EstadoCarga.Error -> Column {
                        Text(c.mensaje, color = MaterialTheme.colorScheme.error)
                        Button(onClick = onReintentar) { Text("Reintentar") }
                    }
                    EstadoCarga.Listo -> Text(
                        if (estado.notas.isEmpty()) "Todavía no hay notas. Tocá + para crear una."
                        else "${estado.notas.size} notas"
                    )
                }
                estado.errorOperacion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
            items(estado.notas, key = { it.id }) { nota ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onAbrir(nota.id) }
                ) {
                    Column(Modifier.padding(start = 12.dp, top = 10.dp, end = 4.dp)) {
                        Text(nota.titulo, style = MaterialTheme.typography.titleMedium)
                        Text(nota.contenido, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onEliminar(nota.id) }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// PANTALLA 2 — DETALLE / EDITOR
// =====================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    estado: DetalleUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    // Estado VISUAL local: si el dialogo esta abierto. No es un dato de la
    // nota ni del negocio, por eso vive en la UI (rememberSaveable: sobrevive
    // a la rotacion).
    var preguntarDescartar by rememberSaveable { mutableStateOf(false) }

    // Volver atras con cambios sin guardar -> preguntar (desafio).
    val intentarVolver = { if (estado.hayCambiosSinGuardar) preguntarDescartar = true else onVolver() }
    BackHandler(enabled = estado.hayCambiosSinGuardar) { preguntarDescartar = true }

    // Cuando el ViewModel confirma que guardo, se vuelve a la lista.
    LaunchedEffect(estado.guardadoOk) { if (estado.guardadoOk) onVolver() }

    if (preguntarDescartar) {
        AlertDialog(
            onDismissRequest = { preguntarDescartar = false },
            title = { Text("¿Descartar cambios?") },
            text = { Text("Tenés cambios sin guardar en esta nota. Si volvés, se pierden.") },
            confirmButton = {
                TextButton(onClick = { preguntarDescartar = false; onVolver() }) { Text("Descartar") }
            },
            dismissButton = {
                TextButton(onClick = { preguntarDescartar = false }) { Text("Seguir editando") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esNueva) "Nueva nota" else "Editar nota") },
                navigationIcon = { TextButton(onClick = intentarVolver) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (val c = estado.carga) {
                EstadoCarga.Cargando -> CircularProgressIndicator()
                is EstadoCarga.Error -> Text(c.mensaje, color = MaterialTheme.colorScheme.error)
                EstadoCarga.Listo -> {
                    OutlinedTextField(
                        value = estado.titulo, onValueChange = onTituloChange, label = { Text("Título") },
                        isError = estado.errorTitulo != null,
                        supportingText = { estado.errorTitulo?.let { Text(it) } },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.contenido, onValueChange = onContenidoChange, label = { Text("Contenido") },
                        isError = estado.errorContenido != null,
                        supportingText = { estado.errorContenido?.let { Text(it) } },
                        minLines = 5, modifier = Modifier.fillMaxWidth()
                    )
                    if (estado.hayCambiosSinGuardar) {
                        Text("● Cambios sin guardar", color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    estado.errorOperacion?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(
                        onClick = onGuardar,
                        enabled = !estado.guardando,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (estado.guardando) "Guardando..." else "Guardar") }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaListaPreview() {
    MvvmLabTheme {
        PantallaLista(
            ListaUiState(
                carga = EstadoCarga.Listo,
                notas = listOf(Nota("1", "Parcial DA1", "Repasar MVVM"), Nota("2", "Compras", "Yerba y mate"))
            ),
            {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaDetallePreview() {
    MvvmLabTheme {
        PantallaDetalle(
            DetalleUiState(
                carga = EstadoCarga.Listo, esNueva = false,
                titulo = "Parcial DA1", contenido = "Repasar MVVM y navegación",
                tituloOriginal = "Parcial DA1", contenidoOriginal = "Repasar MVVM"
            ),
            {}, {}, {}, {}
        )
    }
}
