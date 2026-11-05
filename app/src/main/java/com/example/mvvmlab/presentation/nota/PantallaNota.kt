package com.example.mvvmlab.presentation.nota

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mvvmlab.core.theme.MvvmLabTheme
import com.example.mvvmlab.domain.model.Nota
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- COMPOSABLE CON ESTADO --------------------------------------------
// Es el "dueño" del estado (por ahora; en el Ej 3 el dueño sera el
// ViewModel). Recibe los eventos de la UI y aplica las funciones de arriba.
@Composable
fun PantallaNotaConEstado(modifier: Modifier = Modifier) {
    var estado by remember { mutableStateOf(NotaUiState()) }
    val scope = rememberCoroutineScope()

    PantallaNota(
        estado = estado,
        onTituloChange = { estado = alEditarTitulo(estado, it) },
        onContenidoChange = { estado = alEditarContenido(estado, it) },
        onGuardar = {
            estado = alPulsarGuardar(estado)
            if (estado.guardando) {
                // Guardado SIMULADO: esperamos 1,5 s como si escribiera en disco.
                scope.launch {
                    delay(1500)
                    estado = alTerminarGuardado(estado)
                }
            }
        },
        modifier = modifier
    )
}

// --- COMPOSABLE SIN ESTADO (la View) ----------------------------------
// Solo DIBUJA el estado que recibe y AVISA los eventos con callbacks.
// No decide nada: no valida, no guarda. (State hoisting)
@Composable
fun PantallaNota(
    estado: NotaUiState,
    onTituloChange: (String) -> Unit,
    onContenidoChange: (String) -> Unit,
    onGuardar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Nueva nota", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = estado.titulo,
            onValueChange = onTituloChange,
            label = { Text("Título") },
            isError = estado.error != null,
            supportingText = { estado.error?.let { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.contenido,
            onValueChange = onContenidoChange,
            label = { Text("Contenido") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onGuardar,
            enabled = !estado.guardando,       // evita doble pulsacion mientras guarda
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.guardando) "Guardando..." else "Guardar")
        }

        if (estado.hayCambiosSinGuardar) {
            Text(
                "Hay cambios sin guardar",
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        estado.guardada?.let { nota ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text("Última nota guardada", style = MaterialTheme.typography.labelMedium)
                    Text(nota.titulo, style = MaterialTheme.typography.titleMedium)
                    if (nota.contenido.isNotBlank()) Text(nota.contenido)
                }
            }
        }
    }
}

// --- PREVIEWS ---------------------------------------------------------
// Como PantallaNota no tiene estado propio, se puede previsualizar en
// CUALQUIER situacion pasandole un NotaUiState armado a mano.

@Preview(showBackground = true, name = "1. Vacía")
@Composable
fun PantallaNotaVaciaPreview() {
    MvvmLabTheme {
        PantallaNota(NotaUiState(), {}, {}, {})
    }
}

@Preview(showBackground = true, name = "2. Error de validación")
@Composable
fun PantallaNotaErrorPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(contenido = "Texto sin título", error = "El título no puede estar vacío"),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true, name = "3. Guardando")
@Composable
fun PantallaNotaGuardandoPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(titulo = "Parcial", contenido = "Repasar MVVM", guardando = true),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true, name = "4. Guardada + cambios sin guardar")
@Composable
fun PantallaNotaGuardadaPreview() {
    MvvmLabTheme {
        PantallaNota(
            NotaUiState(
                titulo = "Parcial DA1",
                contenido = "Repasar MVVM y StateFlow",
                guardada = Nota("Parcial", "Repasar MVVM")
            ),
            {}, {}, {}
        )
    }
}
