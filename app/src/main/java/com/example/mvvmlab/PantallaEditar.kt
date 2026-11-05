package com.example.mvvmlab

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditar(
    estado: EditarUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaChange: (Long) -> Unit,
    onFavoritoChange: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit
) {
    // Estados VISUALES locales (no son datos del registro).
    var preguntarDescartar by rememberSaveable { mutableStateOf(false) }
    var elegirFecha by rememberSaveable { mutableStateOf(false) }

    val intentarVolver = { if (estado.hayCambiosSinGuardar) preguntarDescartar = true else onVolver() }
    BackHandler(enabled = estado.hayCambiosSinGuardar) { preguntarDescartar = true }
    LaunchedEffect(estado.guardadoOk) { if (estado.guardadoOk) onVolver() }

    if (preguntarDescartar) {
        AlertDialog(
            onDismissRequest = { preguntarDescartar = false },
            title = { Text("¿Descartar cambios?") },
            text = { Text("Este registro tiene cambios sin guardar.") },
            confirmButton = { TextButton(onClick = { preguntarDescartar = false; onVolver() }) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { preguntarDescartar = false }) { Text("Seguir editando") } }
        )
    }

    if (elegirFecha) {
        val st = rememberDatePickerState(initialSelectedDateMillis = estado.formulario.fecha)
        DatePickerDialog(
            onDismissRequest = { elegirFecha = false },
            confirmButton = {
                TextButton(onClick = { st.selectedDateMillis?.let(onFechaChange); elegirFecha = false }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { elegirFecha = false }) { Text("Cancelar") } }
        ) { DatePicker(state = st) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esNuevo) "Nuevo registro" else "Editar registro") },
                navigationIcon = { TextButton(onClick = intentarVolver) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (val c = estado.carga) {
                EstadoCarga.Cargando -> CircularProgressIndicator()
                is EstadoCarga.Error -> Text(c.mensaje, color = MaterialTheme.colorScheme.error)
                EstadoCarga.Listo -> {
                    val f = estado.formulario
                    OutlinedTextField(
                        value = f.titulo, onValueChange = onTituloChange, label = { Text("Título") },
                        isError = estado.errores.titulo != null,
                        supportingText = { Text(estado.errores.titulo ?: "${f.titulo.trim().length}/${ValidadorRegistro.MAX_TITULO}") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = f.descripcion, onValueChange = onDescripcionChange, label = { Text("Descripción") },
                        isError = estado.errores.descripcion != null,
                        supportingText = { estado.errores.descripcion?.let { Text(it) } },
                        minLines = 4, modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Fecha: ${Fechas.formatear(f.fecha)}", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { elegirFecha = true }) { Text("Cambiar") }
                    }
                    estado.errores.fecha?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Favorito ★", modifier = Modifier.weight(1f))
                        Switch(checked = f.favorito, onCheckedChange = onFavoritoChange)
                    }
                    if (estado.hayCambiosSinGuardar) {
                        Text("● Cambios sin guardar", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Button(onClick = onGuardar, enabled = !estado.guardando, modifier = Modifier.fillMaxWidth()) {
                        Text(if (estado.guardando) "Guardando..." else "Guardar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaEditarPreview() {
    val f = FormularioRegistro("", "Garzas cerca del canal", 1_762_300_800_000, true)
    MvvmLabTheme {
        PantallaEditar(
            EditarUiState(carga = EstadoCarga.Listo, formulario = f, errores = ErroresRegistro(titulo = "El título no puede estar vacío")),
            {}, {}, {}, {}, {}, {}
        )
    }
}
