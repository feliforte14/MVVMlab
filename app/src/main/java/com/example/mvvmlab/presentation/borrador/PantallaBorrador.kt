package com.example.mvvmlab.presentation.borrador

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mvvmlab.core.theme.MvvmLabTheme
import kotlinx.coroutines.flow.StateFlow

// VIEW sin estado: dibuja BorradorUiState y avisa eventos. No sabe que
// existe un archivo: solo ve "texto", "hayCambiosSinGuardar", etc.
@Composable
fun PantallaBorrador(
    estado: BorradorUiState,
    onTextoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDescartarCambios: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Borrador", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            // Distincion VISUAL del borrador sin guardar.
            if (estado.hayCambiosSinGuardar) {
                AssistChip(
                    onClick = {},
                    label = { Text("● Sin guardar") },
                    colors = AssistChipDefaults.assistChipColors(
                        labelColor = MaterialTheme.colorScheme.error
                    )
                )
            } else {
                AssistChip(onClick = {}, label = { Text("✓ Guardado") })
            }
        }
        if (estado.mensaje.isNotEmpty()) {
            Text(estado.mensaje, style = MaterialTheme.typography.bodySmall)
        }
        estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            value = estado.texto,
            onValueChange = onTextoChange,
            label = { Text(if (estado.hayCambiosSinGuardar) "Texto (sin guardar)" else "Texto") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDescartarCambios,
                enabled = estado.hayCambiosSinGuardar && !estado.guardando,
                modifier = Modifier.weight(1f)
            ) { Text("Descartar cambios") }
            Button(
                onClick = onGuardar,
                enabled = estado.hayCambiosSinGuardar && !estado.guardando,
                modifier = Modifier.weight(1f)
            ) { Text(if (estado.guardando) "Guardando..." else "Guardar") }
        }
    }
}

@Preview(showBackground = true, name = "Guardado")
@Composable
fun PantallaBorradorGuardadoPreview() {
    val t = "Ideas para el parcial:\n- MVVM\n- Repository"
    MvvmLabTheme {
        PantallaBorrador(BorradorUiState(texto = t, textoGuardado = t, mensaje = "Cargado desde borrador.txt"), {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Sin guardar")
@Composable
fun PantallaBorradorSinGuardarPreview() {
    MvvmLabTheme {
        PantallaBorrador(
            BorradorUiState(
                texto = "Ideas para el parcial:\n- MVVM\n- Repository\n- StateFlow",
                textoGuardado = "Ideas para el parcial:\n- MVVM\n- Repository",
                mensaje = "Cargado desde borrador.txt"
            ),
            {}, {}, {}
        )
    }
}

// --- PANTALLA CONECTADA AL VIEWMODEL ----------------------------------
@Composable
fun PantallaBorradorConViewModel(viewModel: BorradorViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaBorrador(
        estado = estado,
        onTextoChange = viewModel::onTextoChange,
        onGuardar = viewModel::onGuardar,
        onDescartarCambios = viewModel::onDescartarCambios,
        modifier = modifier
    )
}
