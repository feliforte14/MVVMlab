package com.example.mvvmlab.domain.repository

import com.example.mvvmlab.domain.model.Nota
import kotlinx.coroutines.flow.Flow

// =====================================================================
// EJERCICIO 12 — Contrato con Flow: UNA fuente de verdad para 2 pantallas
// =====================================================================
// En el Ej 6 elegimos "valor puntual" (obtenerTodas) porque habia UNA
// pantalla. Ahora hay dos (lista y detalle): si cada una pidiera su
// "foto" por separado, la lista no se enteraria de lo que guardo el
// detalle. Con observarTodas(): Flow, el Repository EMITE la lista cada
// vez que cambia, y la lista se actualiza sola al volver.
// =====================================================================
interface NotaRepository {
    fun observarTodas(): Flow<List<Nota>>    // emite la lista actual y cada cambio
    suspend fun obtener(id: String): Nota?   // una nota puntual (para el detalle)
    suspend fun guardar(nota: Nota)          // alta o edicion
    suspend fun eliminar(id: String)
}
