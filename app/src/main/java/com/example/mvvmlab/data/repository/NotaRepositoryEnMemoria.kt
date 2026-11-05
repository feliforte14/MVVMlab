package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.NotaRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Implementacion en memoria del MISMO contrato que ArchivoNotasRepository.
// Los datos se pierden al cerrar la app. Se elige desde ContenedorApp.
class NotaRepositoryEnMemoria : NotaRepository {

    private val notas = mutableListOf<Nota>()
    private val mutex = Mutex()

    override suspend fun obtenerTodas(): List<Nota> = mutex.withLock { notas.toList() }

    override suspend fun guardar(nota: Nota) {
        mutex.withLock {
            val i = notas.indexOfFirst { it.id == nota.id }
            if (i >= 0) notas[i] = nota else notas.add(nota)
        }
    }

    override suspend fun eliminar(id: String) {
        mutex.withLock { notas.removeAll { it.id == id } }
    }
}
