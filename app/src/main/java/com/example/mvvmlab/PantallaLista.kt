package com.example.mvvmlab

import android.content.Intent
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.ui.theme.MvvmLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    estado: ListaUiState,
    onBusquedaChange: (String) -> Unit,
    onAbrir: (String) -> Unit,
    onNuevo: () -> Unit,
    onFavorito: (Registro) -> Unit,
    onEliminar: (String) -> Unit,
    onGenerarResumen: (compartir: Boolean) -> Unit,
    onCompartido: () -> Unit,
    onBitacora: () -> Unit,
    onReintentar: () -> Unit
) {
    // API del Android SDK (Ej 12 de la guia basica): Intent ACTION_SEND con
    // el TEXTO del resumen. No se comparte la ruta interna del archivo (que
    // otra app no podria leer de todos modos): se comparte su contenido.
    val context = LocalContext.current
    LaunchedEffect(estado.textoParaCompartir) {
        estado.textoParaCompartir?.let { texto ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Cuaderno de campo — resumen")
                putExtra(Intent.EXTRA_TEXT, texto)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir resumen"))
            onCompartido()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuaderno de campo") },
                actions = { TextButton(onClick = onBitacora) { Text("Bitácora") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevo) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = estado.busqueda,
                    onValueChange = onBusquedaChange,
                    label = { Text("Buscar por título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onGenerarResumen(false) }, modifier = Modifier.weight(1f)) {
                        Text("Generar resumen")
                    }
                    OutlinedButton(onClick = { onGenerarResumen(true) }, modifier = Modifier.weight(1f)) {
                        Text("Compartir")
                    }
                }
                estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall) }
                estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }

            // Estados de carga / vacio / error
            item {
                when (val c = estado.carga) {
                    EstadoCarga.Cargando -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                        Text("  Cargando registros...")
                    }
                    is EstadoCarga.Error -> Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(c.mensaje)
                            Button(onClick = onReintentar) { Text("Reintentar") }
                        }
                    }
                    EstadoCarga.Listo -> when {
                        estado.registros.isEmpty() && estado.busqueda.isBlank() -> MensajeVacio(
                            "Todavía no hay registros.\nTocá + para anotar tu primera observación."
                        )
                        estado.registros.isEmpty() -> MensajeVacio("Ningún registro coincide con “${estado.busqueda}”.")
                        else -> Text("${estado.registros.size} registros", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            items(estado.registros, key = { it.id }) { r ->
                FilaRegistro(r, onAbrir = { onAbrir(r.id) }, onFavorito = { onFavorito(r) }, onEliminar = { onEliminar(r.id) })
            }
        }
    }
}

@Composable
private fun MensajeVacio(texto: String) {
    Text(
        texto,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
    )
}

@Composable
fun FilaRegistro(registro: Registro, onAbrir: () -> Unit, onFavorito: () -> Unit, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir)) {
        Row(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(Fechas.formatear(registro.fecha), style = MaterialTheme.typography.labelMedium)
                Text(registro.titulo, style = MaterialTheme.typography.titleMedium)
                Text(registro.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
            Column(horizontalAlignment = Alignment.End) {
                TextButton(onClick = onFavorito) {
                    Text(if (registro.favorito) "★" else "☆", style = MaterialTheme.typography.titleLarge)
                }
                TextButton(onClick = onEliminar) { Text("Eliminar") }
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
                registros = listOf(
                    Registro("1", "Reserva Costanera Sur", "Garzas y un carpincho cerca del canal", 1_762_300_800_000, true),
                    Registro("2", "Feria de San Telmo", "Mucha gente; comprar yerba", 1_762_214_400_000, false)
                )
            ),
            {}, {}, {}, {}, {}, {}, {}, {}, {}
        )
    }
}

@Preview(showBackground = true, name = "Sin resultados")
@Composable
fun PantallaListaVaciaPreview() {
    MvvmLabTheme {
        PantallaLista(ListaUiState(carga = EstadoCarga.Listo, busqueda = "volcán"), {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}
