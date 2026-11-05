package com.example.mvvmlab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.ui.theme.MvvmLabTheme

// VIEW sin estado: dibuja BorradorUiState y avisa cuando cambia el texto.
// No sabe que existe un archivo.
@Composable
fun PantallaBorrador(
    estado: BorradorUiState,
    onTextoChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Borrador", style = MaterialTheme.typography.headlineSmall)
        if (estado.mensaje.isNotEmpty()) {
            Text(estado.mensaje, style = MaterialTheme.typography.bodySmall)
        }
        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        OutlinedTextField(
            value = estado.texto,
            onValueChange = onTextoChange,
            label = { Text("Texto") },
            minLines = 8,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, name = "Cargado")
@Composable
fun PantallaBorradorCargadoPreview() {
    MvvmLabTheme {
        PantallaBorrador(
            BorradorUiState(texto = "Ideas para el parcial:\n- MVVM\n- Repository", mensaje = "Cargado desde borrador.txt"),
            {}
        )
    }
}

@Preview(showBackground = true, name = "Sin archivo")
@Composable
fun PantallaBorradorVacioPreview() {
    MvvmLabTheme {
        PantallaBorrador(BorradorUiState(mensaje = "Todavía no hay borrador guardado"), {})
    }
}
