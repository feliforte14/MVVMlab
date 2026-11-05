package com.example.mvvmlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.ui.theme.MvvmLabTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaBitacora(
    estado: BitacoraUiState,
    onTextoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDescartar: () -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bitácora") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("← Volver") } }
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Texto libre guardado en ${ArchivoBitacoraRepository.NOMBRE} (independiente de los registros).",
                style = MaterialTheme.typography.bodySmall
            )
            when (val c = estado.carga) {
                EstadoCarga.Cargando -> CircularProgressIndicator()
                is EstadoCarga.Error -> {
                    Text(c.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = onReintentar) { Text("Reintentar") }
                }
                EstadoCarga.Listo -> {
                    OutlinedTextField(
                        value = estado.texto, onValueChange = onTextoChange,
                        label = { Text(if (estado.hayCambiosSinGuardar) "Bitácora (sin guardar)" else "Bitácora") },
                        minLines = 10, modifier = Modifier.fillMaxWidth()
                    )
                    estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                    estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDescartar, enabled = estado.hayCambiosSinGuardar && !estado.guardando,
                            modifier = Modifier.weight(1f)
                        ) { Text("Descartar") }
                        Button(
                            onClick = onGuardar, enabled = estado.hayCambiosSinGuardar && !estado.guardando,
                            modifier = Modifier.weight(1f)
                        ) { Text(if (estado.guardando) "Guardando..." else "Guardar") }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PantallaBitacoraPreview() {
    MvvmLabTheme {
        PantallaBitacora(
            BitacoraUiState(carga = EstadoCarga.Listo, texto = "Sábado: salida a la reserva.\n\nDomingo: lluvia, quedé en casa."),
            {}, {}, {}, {}, {}
        )
    }
}
