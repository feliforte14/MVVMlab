package com.example.mvvmlab

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Implementacion en memoria del MISMO contrato. Los datos se pierden al
// cerrar la app. Se elige desde ContenedorApp.
class NotaRepositoryEnMemoria : NotaRepository {

    private val notas = MutableStateFlow<List<Nota>>(emptyList())

    override fun observarTodas(): Flow<List<Nota>> = notas.asStateFlow()

    override suspend fun obtener(id: String): Nota? = notas.value.find { it.id == id }

    override suspend fun guardar(nota: Nota) {
        notas.update { lista ->
            if (lista.any { it.id == nota.id }) lista.map { if (it.id == nota.id) nota else it }
            else lista + nota
        }
    }

    override suspend fun eliminar(id: String) {
        notas.update { lista -> lista.filterNot { it.id == id } }
    }
}
