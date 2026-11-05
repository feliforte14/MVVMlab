package com.example.mvvmlab

import kotlinx.coroutines.delay

// =====================================================================
// EJERCICIO 6 — Repository: cambiar datos sin cambiar la pantalla
// =====================================================================
//
// CONTRATO (interface): dice QUE operaciones hay, no COMO se hacen.
// El ViewModel solo conoce este contrato. Hoy la implementacion guarda
// en memoria; mañana puede ser un archivo (Ej 7) o Room (Ej 14) y ni el
// ViewModel ni la pantalla se enteran.
//
// Decision: valor puntual (suspend) y no Flow.
//   - suspend fun obtenerTodas(): List<Nota> devuelve una "foto" de las
//     notas en ese momento. Despues de guardar o eliminar, el ViewModel
//     vuelve a pedirla. Es simple y explicito, y alcanza mientras haya
//     UNA sola pantalla que lee y escribe.
//   - Un Flow (que emite solo cada vez que los datos cambian) conviene
//     cuando hay varias pantallas mirando los mismos datos (Ej 12) o una
//     fuente que ya los emite (Room, Ej 14).
//   - suspend: la operacion puede tardar (disco, red) y no debe bloquear
//     la pantalla; se llama desde una corrutina (viewModelScope.launch).
// =====================================================================
interface NotaRepository {
    suspend fun obtenerTodas(): List<Nota>
    suspend fun guardar(nota: Nota)          // si ya existe una nota con ese id, la reemplaza
    suspend fun eliminar(id: String)
}


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
