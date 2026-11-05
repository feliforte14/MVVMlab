package com.example.mvvmlab.presentation.nota

import com.example.mvvmlab.domain.model.Nota

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
