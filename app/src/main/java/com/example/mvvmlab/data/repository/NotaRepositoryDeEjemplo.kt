package com.example.mvvmlab.data.repository

import com.example.mvvmlab.domain.model.Nota
import com.example.mvvmlab.domain.repository.NotaRepository

// --- IMPLEMENTACION 2: precargada (desafio) ----------------------------
// Otra implementacion del MISMO contrato, con datos de ejemplo.
// Para usarla se cambia UNA linea en NotaViewModel (el valor por defecto
// del repositorio). La pantalla (PantallaNota) no se toca.
class NotaRepositoryDeEjemplo : NotaRepository {

    private val notas = mutableListOf(
        Nota("ej-1", "Parcial DA1", "Repasar MVVM, StateFlow y Repository"),
        Nota("ej-2", "TP Rabbit", "Terminar la entrega de testing"),
        Nota("ej-3", "Compras", "Yerba, café y galletitas para estudiar")
    )

    override suspend fun obtenerTodas(): List<Nota> = notas.toList()

    override suspend fun guardar(nota: Nota) {
        val i = notas.indexOfFirst { it.id == nota.id }
        if (i >= 0) notas[i] = nota else notas.add(nota)
    }

    override suspend fun eliminar(id: String) {
        notas.removeAll { it.id == id }
    }
}
