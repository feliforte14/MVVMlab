package com.example.mvvmlab

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
import com.example.mvvmlab.ui.theme.MvvmLabTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
// EJERCICIO 2 — Una pantalla no es su estado
// =====================================================================
//
// Idea central: la pantalla se dibuja a partir de UN UNICO estado
// (NotaUiState). En vez de tener variables sueltas como en el Ej 1,
// todo lo que la pantalla necesita saber esta junto en un objeto.
//
// Todavia NO hay ViewModel (eso es el Ej 3): el estado vive en un
// composable "con estado" (PantallaNotaConEstado). Pero ya separamos:
//   - QUE se ve           -> NotaUiState (datos)
//   - COMO se ve          -> PantallaNota (UI sin estado propio)
//   - QUE pasa ante cada evento -> funciones al final del archivo
// =====================================================================


// --- MODELO -----------------------------------------------------------

// Una nota YA GUARDADA. No es lo mismo que lo que se esta escribiendo:
// el usuario puede tener texto a medio escribir distinto de la ultima
// nota guardada (pregunta 1 de la guia).
data class Nota(
    val titulo: String,
    val contenido: String
)


// --- ESTADO DE PANTALLA -----------------------------------------------

// "Foto" de todo lo que la pantalla necesita para dibujarse.
// Todos los campos son val (inmutables): para cambiar el estado se crea
// una copia nueva con copy(...). Asi cada cambio es un estado nuevo y
// Compose detecta que tiene que redibujar.
data class NotaUiState(
    val titulo: String = "",          // lo que esta escrito en el campo titulo
    val contenido: String = "",       // lo que esta escrito en el campo contenido
    val error: String? = null,        // mensaje de validacion; null = no hay error
    val guardando: Boolean = false,   // true mientras "se guarda" (operacion en curso)
    val guardada: Nota? = null        // ultima nota guardada; null = todavia ninguna
) {
    // Propiedad calculada: no se guarda, se deduce de los otros campos.
    // Sirve para mostrar "cambios sin guardar" sin duplicar informacion.
    val hayCambiosSinGuardar: Boolean
        get() = guardada == null && (titulo.isNotEmpty() || contenido.isNotEmpty()) ||
                guardada != null && (guardada.titulo != titulo || guardada.contenido != contenido)
}


// --- EVENTOS -> CAMBIO DE ESTADO --------------------------------------
// Funciones Kotlin puras: reciben el estado actual y devuelven el nuevo.
// No saben nada de Compose. En el Ej 3 se mudan al ViewModel casi igual.

// Evento: el usuario edita el titulo.
// Se actualiza el titulo y se borra el error (si estaba "titulo vacio",
// al escribir deja de tener sentido mostrarlo).
fun alEditarTitulo(estado: NotaUiState, nuevo: String): NotaUiState =
    estado.copy(titulo = nuevo, error = null)

// Evento: el usuario edita el contenido. Solo cambia el contenido.
fun alEditarContenido(estado: NotaUiState, nuevo: String): NotaUiState =
    estado.copy(contenido = nuevo)

// Evento: el usuario aprieta Guardar.
// Devuelve el estado inmediato: o un error de validacion, o "guardando".
fun alPulsarGuardar(estado: NotaUiState): NotaUiState = when {
    estado.guardando -> estado                                   // ya se esta guardando: ignorar
    estado.titulo.isBlank() -> estado.copy(error = "El título no puede estar vacío")
    else -> estado.copy(guardando = true, error = null)
}

// Resultado de la operacion (simulada): termino de guardar.
fun alTerminarGuardado(estado: NotaUiState): NotaUiState =
    estado.copy(
        guardando = false,
        guardada = Nota(estado.titulo, estado.contenido)
    )


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
