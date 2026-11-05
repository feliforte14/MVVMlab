package com.example.mvvmlab.core.navigation

// Rutas de navegacion: un solo lugar con los nombres de las pantallas y
// sus argumentos (como Screen.kt en el repositorio de referencia).
object Rutas {
    const val LISTA = "lista"
    const val ARG_ID = "id"
    const val DETALLE = "detalle/{$ARG_ID}"

    // Se pasa SOLO el id de la nota (o "nueva"), nunca el objeto entero.
    fun detalle(id: String) = "detalle/$id"
}
