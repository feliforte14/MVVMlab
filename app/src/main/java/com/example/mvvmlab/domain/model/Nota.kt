package com.example.mvvmlab.domain.model

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
