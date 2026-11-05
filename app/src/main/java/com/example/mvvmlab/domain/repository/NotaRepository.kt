package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Nota

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
