package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.NotaRepository
import kotlinx.coroutines.delay

// --- IMPLEMENTACION 1: en memoria (empieza vacia) ----------------------
// Los datos viven en una lista dentro del objeto: se pierden al cerrar
// la app. Sirve para desarrollar la pantalla sin pensar todavia en disco.
class NotaRepositoryEnMemoria : NotaRepository {

    private val notas = mutableListOf<Nota>()

    override suspend fun obtenerTodas(): List<Nota> {
        delay(300)                    // simula una fuente que tarda un poco
        return notas.toList()         // copia: quien la recibe no puede modificar la nuestra
    }

    override suspend fun guardar(nota: Nota) {
        delay(300)
        val i = notas.indexOfFirst { it.id == nota.id }
        if (i >= 0) notas[i] = nota else notas.add(nota)
    }

    override suspend fun eliminar(id: String) {
        delay(300)
        notas.removeAll { it.id == id }
    }
}
