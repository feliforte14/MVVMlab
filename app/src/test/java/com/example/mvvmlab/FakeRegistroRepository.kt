package com.example.mvvmlab

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.io.IOException

// Repository FALSO del integrador (mismo patron que el Ej 13).
class FakeRegistroRepository(iniciales: List<Registro> = emptyList()) : RegistroRepository {

    private val registros = MutableStateFlow(iniciales)
    var fallarLectura = false
    var fallarEscritura = false
    val guardados = mutableListOf<Registro>()

    override fun observar(busqueda: String): Flow<List<Registro>> =
        registros
            .onStart { if (fallarLectura) throw IOException("lectura falsa") }
            .map { lista -> lista.filter { it.titulo.contains(busqueda.trim(), ignoreCase = true) } }

    override suspend fun obtener(id: String) = registros.value.find { it.id == id }

    override suspend fun guardar(registro: Registro) {
        if (fallarEscritura) throw IOException("escritura falsa")
        guardados += registro
        registros.value = registros.value.filterNot { it.id == registro.id } + registro
    }

    override suspend fun eliminar(id: String) {
        if (fallarEscritura) throw IOException("escritura falsa")
        registros.value = registros.value.filterNot { it.id == id }
    }

    override suspend fun cambiarFavorito(id: String, favorito: Boolean) {
        if (fallarEscritura) throw IOException("escritura falsa")
        registros.value = registros.value.map { if (it.id == id) it.copy(favorito = favorito) else it }
    }
}
