package com.example.mvvmlab.presentation.detalle

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.core.theme.MvvmLabTheme

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
