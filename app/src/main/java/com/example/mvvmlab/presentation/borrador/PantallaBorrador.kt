package com.example.mvvmlab.presentation.borrador

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.mvvmlab.core.model.EstadoCarga
import com.example.mvvmlab.core.theme.MvvmLabTheme

@Composable
fun PantallaBorrador(
    estado: BorradorUiState,
    onTextoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDescartarCambios: () -> Unit,
    onReintentar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Borrador", style = MaterialTheme.typography.headlineSmall)

        // when exhaustivo sobre el estado de CARGA: el compilador obliga a
        // contemplar los tres casos.
        when (val carga = estado.carga) {
            EstadoCarga.Cargando -> Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 3.dp)
                Text("Cargando borrador...")
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
                if (estado.existeArchivo) "Borrador cargado" else "Todavía no hay borrador guardado",
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedTextField(
            value = estado.texto,
            onValueChange = onTextoChange,
            enabled = estado.puedeEditar,
            label = { Text(if (estado.hayCambiosSinGuardar) "Texto (sin guardar)" else "Texto") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        // Mensaje del GUARDADO.
        when (val g = estado.guardado) {
            EstadoGuardado.Inactivo -> {}
            EstadoGuardado.Guardando -> Text("Guardando...", style = MaterialTheme.typography.bodySmall)
            EstadoGuardado.Guardado -> Text("✓ Guardado", color = MaterialTheme.colorScheme.primary)
            is EstadoGuardado.Error -> Text(g.mensaje, color = MaterialTheme.colorScheme.error)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDescartarCambios,
                enabled = estado.puedeEditar && estado.hayCambiosSinGuardar,
                modifier = Modifier.weight(1f)
            ) { Text("Descartar cambios") }
            Button(
                onClick = onGuardar,
                enabled = estado.puedeGuardar,
                modifier = Modifier.weight(1f)
            ) { Text(if (estado.guardado == EstadoGuardado.Guardando) "Guardando..." else "Guardar") }
        }
    }
}

@Preview(showBackground = true, name = "Cargando")
@Composable
fun BorradorCargandoPreview() {
    MvvmLabTheme { PantallaBorrador(BorradorUiState(), {}, {}, {}, {}) }
}

@Preview(showBackground = true, name = "Error de carga")
@Composable
fun BorradorErrorPreview() {
    MvvmLabTheme {
        PantallaBorrador(BorradorUiState(carga = EstadoCarga.Error("No se pudo leer el borrador.")), {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Error al guardar")
@Composable
fun BorradorErrorGuardarPreview() {
    MvvmLabTheme {
        PantallaBorrador(
            BorradorUiState(
                carga = EstadoCarga.Listo, texto = "Texto nuevo", textoGuardado = "Texto viejo",
                existeArchivo = true,
                guardado = EstadoGuardado.Error("No se pudo guardar. Tus cambios siguen en pantalla.")
            ),
            {}, {}, {}, {}
        )
    }
}

@Composable
fun PantallaBorradorConViewModel(viewModel: BorradorViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    PantallaBorrador(
        estado = estado,
        onTextoChange = viewModel::onTextoChange,
        onGuardar = viewModel::onGuardar,
        onDescartarCambios = viewModel::onDescartarCambios,
        onReintentar = viewModel::onReintentar,
        modifier = modifier
    )
}
