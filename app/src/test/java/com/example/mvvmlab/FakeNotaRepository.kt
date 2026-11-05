package com.example.mvvmlab

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import java.io.IOException

// =====================================================================
// EJERCICIO 13 — Repository FALSO para pruebas
// =====================================================================
// Implementa el MISMO contrato que la app, pero:
//   - arranca con datos prefijados (sin archivos, sin Android),
//   - se le puede pedir que FALLE a proposito,
//   - REGISTRA las llamadas, para verificar que algo se llamo (o no).
// Vive en src/test: no forma parte de la app instalada.
// =====================================================================
class FakeNotaRepository(
    notasIniciales: List<Nota> = emptyList()
) : NotaRepository {

    private val notas = MutableStateFlow(notasIniciales)

    // Interruptores de fallas
    var fallarLectura = false
    var fallarEscritura = false

    // Registro de llamadas
    val guardadas = mutableListOf<Nota>()
    val eliminadas = mutableListOf<String>()

    override fun observarTodas(): Flow<List<Nota>> =
        notas.onStart { if (fallarLectura) throw IOException("Lectura falsa fallida") }

    override suspend fun obtener(id: String): Nota? {
        if (fallarLectura) throw IOException("Lectura falsa fallida")
        return notas.value.find { it.id == id }
    }

    override suspend fun guardar(nota: Nota) {
        if (fallarEscritura) throw IOException("Escritura falsa fallida")
        guardadas += nota
        notas.value = notas.value.filterNot { it.id == nota.id } + nota
    }

    override suspend fun eliminar(id: String) {
        if (fallarEscritura) throw IOException("Escritura falsa fallida")
        eliminadas += id
        notas.value = notas.value.filterNot { it.id == id }
    }
}
