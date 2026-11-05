package com.example.mvvmlab.core.navigation

// Rutas de navegacion: un solo lugar con los nombres de las pantallas y
// sus argumentos (como Screen.kt en el repositorio de referencia).
object Rutas {
    const val LISTA = "lista"
    const val BITACORA = "bitacora"
    const val ARG_ID = "id"
    const val EDITAR = "editar/{$ARG_ID}"

    // Se pasa SOLO el id del registro (o "nuevo"), nunca el objeto entero.
    fun editar(id: String) = "editar/$id"
}
