package com.example.mvvmlab.presentation.lista

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.domain.model.Nota

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
    onReintentar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis notas") }) },
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
